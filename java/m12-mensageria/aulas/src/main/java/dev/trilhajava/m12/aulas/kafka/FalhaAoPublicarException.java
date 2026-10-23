package dev.trilhajava.m12.aulas.kafka;

/** O broker não confirmou a gravação do evento dentro do prazo. */
public class FalhaAoPublicarException extends RuntimeException {

    public FalhaAoPublicarException(EventoDePedido evento, Throwable causa) {
        super("não foi possível publicar %s do pedido %s".formatted(evento.status(), evento.numeroPedido()), causa);
    }
}
