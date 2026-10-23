package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Papel;
import dev.trilhajava.fintech.dominio.RepositorioDeUsuarios;
import dev.trilhajava.fintech.dominio.Usuario;
import dev.trilhajava.fintech.dominio.UsuarioDuplicadoException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeUsuarios {

    static final int SENHA_MINIMA = 12;
    /** O BCrypt só considera 72 BYTES; 64 caracteres (e no máximo 72 bytes em UTF-8) cabem com folga. */
    static final int SENHA_MAXIMA = 64;
    static final int BYTES_MAXIMOS = 72;

    private final RepositorioDeUsuarios usuarios;
    private final PasswordEncoder codificador;
    private final Clock clock;

    public ServicoDeUsuarios(RepositorioDeUsuarios usuarios, PasswordEncoder codificador, Clock clock) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.clock = clock;
    }

    /** Cadastro público: sempre CLIENTE — o papel nunca vem do corpo da requisição. */
    @Transactional
    public Usuario cadastrar(String nome, String email, String senha) {
        return criar(nome, email, senha, Papel.CLIENTE);
    }

    @Transactional
    public Usuario criar(String nome, String email, String senha, Papel papel) {
        // A API já valida o tamanho; conferir aqui também protege os outros caminhos (ex.: o admin inicial).
        if (senha == null || senha.length() < SENHA_MINIMA || senha.length() > SENHA_MAXIMA
                || senha.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS) {
            throw new IllegalArgumentException(
                    "A senha deve ter entre " + SENHA_MINIMA + " e " + SENHA_MAXIMA + " caracteres (até "
                            + BYTES_MAXIMOS + " bytes)");
        }
        String normalizado = Usuario.normalizarEmail(email);
        if (usuarios.existePorEmail(normalizado)) {
            throw new UsuarioDuplicadoException();
        }
        var usuario = new Usuario(UUID.randomUUID(), nome.strip(), normalizado, codificador.encode(senha), papel,
                clock.instant());
        try {
            usuarios.salvar(usuario);
        } catch (DataIntegrityViolationException corrida) {
            throw new UsuarioDuplicadoException();
        }
        return usuario;
    }
}
