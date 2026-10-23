package dev.trilhajava.m12.notificacoes;

/** Falha permanente: a mensagem nunca vai dar certo, por mais que se tente. */
public class DestinatarioInvalidoException extends RuntimeException {

    public DestinatarioInvalidoException(String destinatario) {
        super("destinatário inválido: " + destinatario);
    }
}
