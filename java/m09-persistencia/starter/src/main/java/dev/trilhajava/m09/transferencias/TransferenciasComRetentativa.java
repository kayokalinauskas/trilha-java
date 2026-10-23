package dev.trilhajava.m09.transferencias;

import java.math.BigDecimal;

/**
 * Chama ServicoDeTransferencias.transferir e, em caso de conflito de concorrência
 * (OptimisticLockingFailureException), tenta de novo — até 10 vezes, com uma pequena espera.
 * Esgotadas as tentativas: TransferenciaConcorrenteException.
 */
public class TransferenciasComRetentativa {

    public void transferir(long origem, long destino, BigDecimal valor) {
        throw new UnsupportedOperationException("TODO");
    }
}
