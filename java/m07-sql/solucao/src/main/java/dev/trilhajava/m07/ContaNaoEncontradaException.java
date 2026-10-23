package dev.trilhajava.m07;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(long contaId) {
        super("Conta não encontrada: " + contaId);
    }
}
