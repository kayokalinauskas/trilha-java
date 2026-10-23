package dev.trilhajava.m10.arquitetura.exemplos.violacao.injecao.aplicacao;

import dev.trilhajava.m10.arquitetura.exemplos.violacao.injecao.dominio.conta.Conta;
import dev.trilhajava.m10.arquitetura.exemplos.violacao.injecao.dominio.conta.RepositorioDeContas;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Exemplo analisado pelo ArchUnit (violação: injecao). */
@Service
public class ServicoDeContas {

    @Autowired
    private RepositorioDeContas contas;

    @Transactional
    public Conta depositar(long contaId, BigDecimal valor) {
        Conta conta = contas.buscar(contaId).orElseThrow();
        conta.depositar(valor);
        contas.salvar(conta);
        return conta;
    }
}
