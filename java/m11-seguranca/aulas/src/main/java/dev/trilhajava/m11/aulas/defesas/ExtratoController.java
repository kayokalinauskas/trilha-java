package dev.trilhajava.m11.aulas.defesas;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExtratoController {

    static final int TAMANHO_MAXIMO = 100;

    /** 250 lançamentos fictícios, iguais para todo mundo: o foco da aula não é o banco. */
    private static final List<Lancamento> LANCAMENTOS = IntStream.rangeClosed(1, 250)
            .mapToObj(i -> new Lancamento(LocalDate.of(2026, 1, 1).plusDays(i),
                    i % 2 == 0 ? "Pix recebido" : "Compra no débito",
                    new BigDecimal(i % 2 == 0 ? "150.00" : "-42.90")))
            .toList();

    private final ServicoDeExportacao exportacoes;

    public ExtratoController(ServicoDeExportacao exportacoes) {
        this.exportacoes = exportacoes;
    }

    /** Rota pública: o limite, aqui, é por IP. */
    @GetMapping("/cotacoes")
    Map<String, BigDecimal> cotacoes() {
        return Map.of("USD", new BigDecimal("5.4210"), "EUR", new BigDecimal("6.3105"));
    }

    @GetMapping("/extratos")
    PaginaDeExtrato extrato(@RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(TAMANHO_MAXIMO) int tamanho) {
        int inicio = (int) Math.min((long) pagina * tamanho, LANCAMENTOS.size());
        int fim = Math.min(inicio + tamanho, LANCAMENTOS.size());
        return new PaginaDeExtrato(pagina, tamanho, LANCAMENTOS.size(), LANCAMENTOS.subList(inicio, fim));
    }

    @PostMapping("/exportacoes")
    ResponseEntity<Exportacao> exportar(Principal usuario,
            @RequestHeader("Idempotency-Key") String chave,
            @RequestBody @Valid PedidoDeExportacao pedido) {
        ServicoDeExportacao.Resultado resultado = exportacoes.exportar(usuario.getName(), chave, pedido);
        Exportacao exportacao = resultado.exportacao();
        return ResponseEntity.accepted()
                .location(URI.create("/api/exportacoes/" + exportacao.id()))
                .header("Idempotent-Replayed", String.valueOf(resultado.repetida()))
                .body(exportacao);
    }
}
