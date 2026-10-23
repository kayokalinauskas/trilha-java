package dev.trilhajava.m11.aulas.senhas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * "Esqueci a senha" com token de uso único.
 *
 * <ul>
 *   <li>o token tem 256 bits do {@link SecureRandom} e vai só no e-mail;</li>
 *   <li>o servidor guarda apenas o SHA-256 do token: quem lê o banco não consegue usá-lo;</li>
 *   <li>vale 20 minutos, uma única vez, e um pedido novo invalida o anterior;</li>
 *   <li>a resposta de {@link #solicitar} é a mesma para e-mail cadastrado ou não.</li>
 * </ul>
 */
public class RecuperacaoDeSenha {

    public enum Resultado { REDEFINIDA, TOKEN_INVALIDO }

    static final Duration VALIDADE = Duration.ofMinutes(20);
    static final String LINK = "https://portal.clinica.example/redefinir-senha?token=";

    private record Pedido(String email, Instant expiraEm) {
    }

    private final RepositorioDeContas contas;
    private final PasswordEncoder codificador;
    private final PoliticaDeSenha politica;
    private final ControleDeTentativas tentativas;
    private final CaixaDeEmail emails;
    private final Clock clock;
    private final SecureRandom aleatorio = new SecureRandom();
    /** Chave: SHA-256 do token, em hexadecimal. O token em si não fica guardado em lugar nenhum. */
    private final Map<String, Pedido> pedidos = new ConcurrentHashMap<>();

    public RecuperacaoDeSenha(RepositorioDeContas contas, PasswordEncoder codificador, PoliticaDeSenha politica,
            ControleDeTentativas tentativas, CaixaDeEmail emails, Clock clock) {
        this.contas = contas;
        this.codificador = codificador;
        this.politica = politica;
        this.tentativas = tentativas;
        this.emails = emails;
        this.clock = clock;
    }

    public void solicitar(String email) {
        String normalizado = ServicoDeAcesso.normalizar(email);
        if (contas.buscar(normalizado).isEmpty()) {
            return; // a tela diz "se o e-mail estiver cadastrado, você vai receber um link"
        }
        pedidos.values().removeIf(pedido -> pedido.email().equals(normalizado));
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        pedidos.put(sha256(token), new Pedido(normalizado, clock.instant().plus(VALIDADE)));
        emails.enviar(normalizado, "Redefinição de senha",
                "Para criar uma senha nova, abra em até 20 minutos: " + LINK + token);
    }

    public Resultado redefinir(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            return Resultado.TOKEN_INVALIDO;
        }
        String chave = sha256(token);
        Pedido pedido = pedidos.get(chave);
        if (pedido == null || !clock.instant().isBefore(pedido.expiraEm())) {
            return Resultado.TOKEN_INVALIDO;
        }
        List<PoliticaDeSenha.Problema> problemas = politica.avaliar(pedido.email(), novaSenha);
        if (!problemas.isEmpty()) {
            throw new SenhaRecusadaException(problemas); // o token continua valendo para tentar outra
        }
        // remove(chave, valor) é atômico: de duas requisições com o mesmo token, só uma passa.
        if (!pedidos.remove(chave, pedido)) {
            return Resultado.TOKEN_INVALIDO;
        }
        contas.salvar(new Conta(pedido.email(), codificador.encode(novaSenha)));
        tentativas.limpar(pedido.email());
        emails.enviar(pedido.email(), "Sua senha foi alterada",
                "Se não foi você, fale com a clínica imediatamente.");
        return Resultado.REDEFINIDA;
    }

    static String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Toda JVM tem SHA-256", e);
        }
    }
}
