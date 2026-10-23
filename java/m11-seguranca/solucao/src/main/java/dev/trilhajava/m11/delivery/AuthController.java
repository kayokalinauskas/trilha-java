package dev.trilhajava.m11.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    public record Credenciais(@NotBlank String usuario, @NotBlank String senha) {
    }

    private final AuthenticationManager autenticacao;
    private final EmissorDeTokens emissor;

    public AuthController(AuthenticationManager autenticacao, EmissorDeTokens emissor) {
        this.autenticacao = autenticacao;
        this.emissor = emissor;
    }

    @PostMapping("/auth/token")
    public ResponseEntity<?> token(@Valid @RequestBody Credenciais credenciais) {
        try {
            Authentication autenticado = autenticacao.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(credenciais.usuario(), credenciais.senha()));
            return ResponseEntity.ok(emissor.emitir(autenticado));
        } catch (AuthenticationException erro) {
            // A MESMA resposta para usuário inexistente e senha errada.
            ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
            problema.setType(URI.create("https://api.delivery.dev/problemas/credenciais-invalidas"));
            problema.setTitle("Credenciais inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problema);
        }
    }
}
