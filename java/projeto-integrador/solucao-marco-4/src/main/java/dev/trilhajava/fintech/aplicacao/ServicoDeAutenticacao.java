package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.RepositorioDeUsuarios;
import dev.trilhajava.fintech.dominio.Usuario;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeAutenticacao {

    private final RepositorioDeUsuarios usuarios;
    private final PasswordEncoder codificador;
    private final EmissorDeTokens emissor;
    /** Comparado quando o e-mail não existe, para o tempo de resposta não revelar quais e-mails existem. */
    private final String hashFicticio;

    public ServicoDeAutenticacao(RepositorioDeUsuarios usuarios, PasswordEncoder codificador, EmissorDeTokens emissor) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emissor = emissor;
        this.hashFicticio = codificador.encode("senha-ficticia-so-para-igualar-o-tempo");
    }

    @Transactional(readOnly = true)
    public TokenEmitido autenticar(String email, String senha) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > ServicoDeUsuarios.BYTES_MAXIMOS) {
            // Nenhuma senha cadastrada passa de 72 bytes — e o BCrypt truncaria esta ao comparar.
            throw new CredenciaisInvalidasException();
        }
        Optional<Usuario> usuario = usuarios.buscarPorEmail(Usuario.normalizarEmail(email));
        boolean senhaConfere = codificador.matches(senha, usuario.map(Usuario::senhaHash).orElse(hashFicticio));
        if (usuario.isEmpty() || !senhaConfere) {
            throw new CredenciaisInvalidasException();
        }
        return emissor.emitir(usuario.get());
    }
}
