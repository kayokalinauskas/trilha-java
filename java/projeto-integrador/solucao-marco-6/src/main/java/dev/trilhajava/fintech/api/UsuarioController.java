package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.aplicacao.ServicoDeUsuarios;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Cadastro de clientes")
public class UsuarioController {

    private final ServicoDeUsuarios servico;

    public UsuarioController(ServicoDeUsuarios servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Cadastra um cliente", description = "Rota pública. Senha com 12 a 64 caracteres.")
    public UsuarioResponse cadastrar(@Valid @RequestBody NovoUsuarioRequest pedido) {
        return UsuarioResponse.de(servico.cadastrar(pedido.nome(), pedido.email(), pedido.senha()));
    }
}
