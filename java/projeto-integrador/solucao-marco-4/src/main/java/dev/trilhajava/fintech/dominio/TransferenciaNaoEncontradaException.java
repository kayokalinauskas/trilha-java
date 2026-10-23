package dev.trilhajava.fintech.dominio;

public class TransferenciaNaoEncontradaException extends RuntimeException {

    public TransferenciaNaoEncontradaException(java.util.UUID id) {
        super("Transferência " + id + " não encontrada");
    }
}
