package dev.trilhajava.m13.aulas.padroes;

/** O gateway não respondeu agora, mas pode responder se você tentar de novo. */
public class FalhaTemporariaException extends RuntimeException {

    public FalhaTemporariaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
