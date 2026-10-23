package dev.trilhajava.m09.transferencias;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(long contaId) {
        super("Saldo insuficiente na conta " + contaId);
    }
}
