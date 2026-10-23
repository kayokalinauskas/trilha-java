package dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.api;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.infra.RepositorioDeContasEmMemoria;
import dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.dominio.conta.Conta;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exemplo analisado pelo ArchUnit (violação: camadas). */
@RestController
public class ContaController {

    private final RepositorioDeContasEmMemoria repositorio;

    public ContaController(RepositorioDeContasEmMemoria repositorio) {
        this.repositorio = repositorio;
    }

    @PostMapping("/contas/{id}/depositos")
    public BigDecimal depositar(@PathVariable long id, @RequestParam BigDecimal valor) {
        Conta conta = repositorio.buscar(id).orElseThrow();
        conta.depositar(valor);
        repositorio.salvar(conta);
        return conta.saldo();
    }
}
