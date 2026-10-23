package dev.trilhajava.m10.arquitetura.exemplos.violacao.transacao.aplicacao;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.transacao.dominio.conta.Conta;
import dev.trilhajava.m10.arquitetura.exemplos.violacao.transacao.dominio.conta.RepositorioDeContas;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Exemplo analisado pelo ArchUnit (violação: transacao). */
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
