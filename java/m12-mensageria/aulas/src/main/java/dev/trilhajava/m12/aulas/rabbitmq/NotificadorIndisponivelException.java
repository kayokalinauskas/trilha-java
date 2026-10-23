package dev.trilhajava.m12.aulas.rabbitmq;

/** Falha transitória: o provedor de SMS ou e-mail não respondeu. */
public class NotificadorIndisponivelException extends RuntimeException {

    public NotificadorIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
