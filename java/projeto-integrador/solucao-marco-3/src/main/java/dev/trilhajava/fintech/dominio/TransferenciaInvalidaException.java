package dev.trilhajava.fintech.dominio;

public class TransferenciaInvalidaException extends RuntimeException {

    public TransferenciaInvalidaException(String motivo) {
        super(motivo);
    }
}
