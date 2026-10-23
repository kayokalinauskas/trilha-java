package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.aplicacao.ServicoDeContas;
import dev.trilhajava.fintech.dominio.Conta;
import dev.trilhajava.fintech.dominio.Cpf;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Abertura, consulta e movimentação de contas")
public class ContaController {

    private final ServicoDeContas servico;

    public ContaController(ServicoDeContas servico) {
        this.servico = servico;
    }

    @PostMapping
    @Operation(summary = "Abre uma conta", description = "Uma pessoa pode ter no máximo uma conta de cada tipo.")
    public ResponseEntity<ContaResponse> abrir(@Valid @RequestBody NovaContaRequest pedido, UriComponentsBuilder uri) {
        Conta conta = servico.abrir(pedido.titular(), new Cpf(pedido.cpf()), pedido.tipo());
        return ResponseEntity.created(uri.path("/contas/{id}").buildAndExpand(conta.id()).toUri())
                .body(ContaResponse.de(conta));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma conta")
    public ContaResponse buscar(@PathVariable UUID id) {
        return ContaResponse.de(servico.buscar(id));
    }

    @GetMapping
    @Operation(summary = "Lista contas, opcionalmente filtrando por CPF")
    public PaginaResponse<ContaResponse> listar(
            @RequestParam(required = false) @CpfValido String cpf,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        Cpf filtro = cpf == null ? null : new Cpf(cpf);
        return PaginaResponse.de(servico.listar(filtro, pagina, tamanho).map(ContaResponse::de));
    }

    @PostMapping("/{id}/depositos")
    @Operation(summary = "Deposita um valor na conta")
    public ContaResponse depositar(@PathVariable UUID id, @Valid @RequestBody ValorRequest pedido) {
        return ContaResponse.de(servico.depositar(id, pedido.valor()));
    }

    @PostMapping("/{id}/saques")
    @Operation(summary = "Saca um valor da conta")
    public ContaResponse sacar(@PathVariable UUID id, @Valid @RequestBody ValorRequest pedido) {
        return ContaResponse.de(servico.sacar(id, pedido.valor()));
    }

    @GetMapping("/{id}/extrato")
    @Operation(summary = "Extrato com todos os movimentos, do mais antigo ao mais recente")
    public ExtratoResponse extrato(@PathVariable UUID id) {
        return ExtratoResponse.de(servico.buscar(id));
    }
}
