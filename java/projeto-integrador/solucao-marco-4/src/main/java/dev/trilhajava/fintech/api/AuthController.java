package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.aplicacao.ServicoDeAutenticacao;
import dev.trilhajava.fintech.aplicacao.TokenEmitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticação")
public class AuthController {

    private final ServicoDeAutenticacao servico;

    public AuthController(ServicoDeAutenticacao servico) {
        this.servico = servico;
    }

    @PostMapping("/auth/token")
    @SecurityRequirements
    @Operation(summary = "Troca e-mail e senha por um access token (JWT)")
    public TokenResponse token(@Valid @RequestBody LoginRequest pedido) {
        TokenEmitido token = servico.autenticar(pedido.email(), pedido.senha());
        return new TokenResponse(token.valor(), "Bearer", token.validade().toSeconds());
    }
}
