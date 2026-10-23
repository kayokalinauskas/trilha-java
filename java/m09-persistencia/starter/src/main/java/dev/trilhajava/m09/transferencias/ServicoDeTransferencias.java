package dev.trilhajava.m09.transferencias;

import java.math.BigDecimal;

/**
 * Exercício 09.03 — crie a entidade Conta (tabela em db/transferencias/V1__cria_conta.sql),
 * o repositório e implemente as duas estratégias de concorrência.
 */
public class ServicoDeTransferencias {

    /** Lock OTIMISTA (@Version). Em caso de conflito, deixe a exceção de concorrência sair. */
    public void transferir(long origem, long destino, BigDecimal valor) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Lock PESSIMISTA (SELECT ... FOR UPDATE), sem risco de deadlock. */
    public void transferirComLockPessimista(long origem, long destino, BigDecimal valor) {
        throw new UnsupportedOperationException("TODO");
    }
}
