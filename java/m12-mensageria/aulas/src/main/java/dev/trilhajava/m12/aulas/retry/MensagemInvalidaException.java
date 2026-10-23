package dev.trilhajava.m12.aulas.retry;

/** Falha PERMANENTE: a mensagem nunca vai ser processada, por mais que se tente. Vai direto para o DLT. */
public class MensagemInvalidaException extends RuntimeException {

    public MensagemInvalidaException(String mensagem) {
        super(mensagem);
    }
}
