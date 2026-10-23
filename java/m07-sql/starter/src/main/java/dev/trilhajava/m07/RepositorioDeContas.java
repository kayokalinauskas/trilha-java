package dev.trilhajava.m07;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Exercício 07.04 — Repositório com JDBC puro (sem JPA ainda).
 * Rode: mvn test -Dtest=RepositorioDeContasTest
 *
 * Regras: sempre PreparedStatement (nunca concatenar valores no SQL), sempre try-with-resources,
 * e a transferência é UMA transação (commit no fim, rollback em qualquer erro).
 * SQLException vira IllegalStateException (unchecked) com a causa preservada.
 */
public class RepositorioDeContas {

    public RepositorioDeContas(DataSource dataSource) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Insere a conta e devolve o id gerado pelo banco. */
    public long abrir(String titular, BigDecimal saldoInicial) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<BigDecimal> saldo(long contaId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Movimentos da conta, na ordem em que foram gravados. */
    public List<Movimento> movimentos(long contaId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Debita a origem, credita o destino e grava um movimento em cada conta, atomicamente.
     * Descrições: "Transferência enviada para a conta X" e "Transferência recebida da conta Y".
     */
    public void transferir(long origem, long destino, BigDecimal valor) {
        throw new UnsupportedOperationException("TODO");
    }
}
