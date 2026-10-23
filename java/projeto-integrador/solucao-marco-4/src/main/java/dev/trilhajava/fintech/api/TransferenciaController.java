package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.aplicacao.ServicoDeTransferencias;
import dev.trilhajava.fintech.aplicacao.ServicoDeTransferencias.Resultado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/transferencias")
@Tag(name = "Transferências", description = "Transferências entre contas, atômicas e idempotentes")
public class TransferenciaController {

    private final ServicoDeTransferencias servico;

    public TransferenciaController(ServicoDeTransferencias servico) {
        this.servico = servico;
    }

    @PostMapping
    @Operation(summary = "Transfere entre duas contas",
            description = "Repetir a requisição com a mesma Idempotency-Key devolve a resposta original sem transferir de novo.")
    public ResponseEntity<TransferenciaResponse> transferir(
            @Parameter(description = "Identificador único desta intenção de transferência (ex.: um UUID gerado pelo app)")
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String chave,
            @Valid @RequestBody NovaTransferenciaRequest pedido,
            UriComponentsBuilder uri,
            Authentication autenticacao) {
        Resultado resultado = servico.transferir(Solicitantes.de(autenticacao), chave, pedido.origem(), pedido.destino(), pedido.valor());
        var resposta = ResponseEntity.created(
                uri.path("/transferencias/{id}").buildAndExpand(resultado.transferencia().id()).toUri());
        if (resultado.repetida()) {
            resposta.header("Idempotent-Replayed", "true");
        }
        return resposta.body(TransferenciaResponse.de(resultado.transferencia()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma transferência")
    public TransferenciaResponse buscar(@PathVariable UUID id, Authentication autenticacao) {
        return TransferenciaResponse.de(servico.buscar(Solicitantes.de(autenticacao), id));
    }
}
