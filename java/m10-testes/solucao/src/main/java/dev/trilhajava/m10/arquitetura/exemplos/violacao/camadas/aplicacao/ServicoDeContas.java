package dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.aplicacao;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.dominio.conta.Conta;
import dev.trilhajava.m10.arquitetura.exemplos.violacao.camadas.dominio.conta.RepositorioDeContas;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Exemplo analisado pelo ArchUnit (violação: camadas). */
@Service
public class ServicoDeContas {

    private final RepositorioDeContas contas;

    public ServicoDeContas(RepositorioDeContas contas) {
        this.contas = contas;
    }

    @Transactional
    public Conta depositar(long contaId, BigDecimal valor) {
        Conta conta = contas.buscar(contaId).orElseThrow();
        conta.depositar(valor);
        contas.salvar(conta);
        return conta;
    }
}
