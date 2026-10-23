package dev.trilhajava.m10.arquitetura.exemplos.violacao.dominio.api;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.dominio.aplicacao.ServicoDeContas;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exemplo analisado pelo ArchUnit (violação: dominio). */
@RestController
public class ContaController {

    private final ServicoDeContas servico;

    public ContaController(ServicoDeContas servico) {
        this.servico = servico;
    }

    @PostMapping("/contas/{id}/depositos")
    public BigDecimal depositar(@PathVariable long id, @RequestParam BigDecimal valor) {
        return servico.depositar(id, valor).saldo();
    }
}
