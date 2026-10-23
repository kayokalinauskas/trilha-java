package dev.trilhajava.m07;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(long contaId) {
        super("Saldo insuficiente na conta " + contaId);
    }
}
