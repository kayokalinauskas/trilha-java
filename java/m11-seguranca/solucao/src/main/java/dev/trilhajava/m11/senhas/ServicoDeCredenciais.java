package dev.trilhajava.m11.senhas;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cadastro e verificação de senhas seguindo as recomendações atuais (NIST SP 800-63B e OWASP):
 * tamanho mínimo generoso, sem regras de composição, bloqueio de senhas vazadas, hash lento com
 * salt, bloqueio temporário contra força bruta e migração transparente de hashes legados.
 */
public class ServicoDeCredenciais {

    static final int TAMANHO_MINIMO = 12;
    /** O BCrypt só usa os primeiros 72 BYTES da senha (e o Spring Security 7 recusa mais que isso no encode). */
    static final int TAMANHO_MAXIMO = 64;
    static final int BYTES_MAXIMOS = 72;
    static final int MAXIMO_DE_FALHAS = 5;
    static final Duration TEMPO_DE_BLOQUEIO = Duration.ofMinutes(15);

    private final RepositorioDeUsuarios usuarios;
    private final PasswordEncoder codificador;
    private final Clock clock;
    private final Set<String> senhasVazadas;
    /** Hash de uma senha qualquer: usado para gastar o MESMO tempo quando o e-mail não existe. */
    private final String hashFicticio;

    public ServicoDeCredenciais(RepositorioDeUsuarios usuarios, PasswordEncoder codificador, Clock clock,
            Set<String> senhasVazadas) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.clock = clock;
        this.senhasVazadas = senhasVazadas.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        this.hashFicticio = codificador.encode("senha-ficticia-para-equalizar-o-tempo");
    }

    public void cadastrar(String email, String senha) {
        String normalizado = normalizar(email);
        validarPolitica(normalizado, senha);
        if (usuarios.buscar(normalizado).isPresent()) {
            throw new EmailJaCadastradoException();
        }
        usuarios.salvar(Usuario.novo(normalizado, codificador.encode(senha)));
    }

    public ResultadoLogin autenticar(String email, String senha) {
        String normalizado = normalizar(email);
        if (senha == null || senha.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS) {
            // Nenhuma senha cadastrada é tão longa — e o BCrypt truncaria esta em 72 bytes ao comparar.
            codificador.matches("", hashFicticio);
            return ResultadoLogin.CREDENCIAIS_INVALIDAS;
        }
        var encontrado = usuarios.buscar(normalizado);
        if (encontrado.isEmpty()) {
            codificador.matches(senha, hashFicticio); // mesmo custo: o tempo de resposta não revela se o e-mail existe
            return ResultadoLogin.CREDENCIAIS_INVALIDAS;
        }
        Usuario usuario = encontrado.get();
        Instant agora = clock.instant();
        if (usuario.bloqueadoAte() != null && agora.isBefore(usuario.bloqueadoAte())) {
            return ResultadoLogin.BLOQUEADO;
        }
        if (!codificador.matches(senha, usuario.hashDaSenha())) {
            registrarFalha(usuario, agora);
            return ResultadoLogin.CREDENCIAIS_INVALIDAS;
        }
        // Sucesso: zera as falhas e, se o hash for de um algoritmo antigo, regrava com o atual.
        String hash = codificador.upgradeEncoding(usuario.hashDaSenha()) ? codificador.encode(senha) : usuario.hashDaSenha();
        usuarios.salvar(new Usuario(usuario.email(), hash, 0, null));
        return ResultadoLogin.SUCESSO;
    }

    private void registrarFalha(Usuario usuario, Instant agora) {
        int falhas = usuario.falhasSeguidas() + 1;
        if (falhas >= MAXIMO_DE_FALHAS) {
            usuarios.salvar(new Usuario(usuario.email(), usuario.hashDaSenha(), 0, agora.plus(TEMPO_DE_BLOQUEIO)));
        } else {
            usuarios.salvar(new Usuario(usuario.email(), usuario.hashDaSenha(), falhas, null));
        }
    }

    private void validarPolitica(String email, String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            throw new SenhaFracaException("A senha precisa ter pelo menos " + TAMANHO_MINIMO + " caracteres");
        }
        if (senha.length() > TAMANHO_MAXIMO || senha.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS) {
            throw new SenhaFracaException("A senha pode ter no máximo " + TAMANHO_MAXIMO + " caracteres ("
                    + BYTES_MAXIMOS + " bytes)");
        }
        String minuscula = senha.toLowerCase(Locale.ROOT);
        if (senhasVazadas.contains(minuscula)) {
            throw new SenhaFracaException("Esta senha aparece em vazamentos conhecidos");
        }
        String usuarioDoEmail = email.substring(0, email.indexOf('@'));
        if (usuarioDoEmail.length() >= 3 && minuscula.contains(usuarioDoEmail)) {
            throw new SenhaFracaException("A senha não pode conter o seu e-mail");
        }
    }

    /** Regra 1: sem espaços nas pontas, minúsculo, exatamente um "@" com algo antes e depois, sem espaços no meio. */
    private static String normalizar(String email) {
        if (email == null) {
            throw new IllegalArgumentException("E-mail inválido");
        }
        String limpo = email.strip().toLowerCase(Locale.ROOT);
        int arroba = limpo.indexOf('@');
        boolean umArrobaNoMeio = arroba > 0 && arroba == limpo.lastIndexOf('@') && arroba < limpo.length() - 1;
        if (!umArrobaNoMeio || temEspaco(limpo)) {
            throw new IllegalArgumentException("E-mail inválido");
        }
        return limpo;
    }

    private static boolean temEspaco(String texto) {
        for (char c : texto.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
