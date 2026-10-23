package dev.trilhajava.m09.transferencias;

public class ContaNaoEncontradaException extends RuntimeException {

    public ContaNaoEncontradaException(long contaId) {
        super("Conta " + contaId + " não encontrada");
    }
}
