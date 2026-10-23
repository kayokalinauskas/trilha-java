package dev.trilhajava.m12.aulas.fundamentos;

import java.time.Instant;
import java.util.UUID;

/**
 * Um fato que já aconteceu. O nome fica no passado ({@code PedidoPago}), quem publica
 * não sabe quem vai consumir e ninguém pode "recusar" um evento: o pedido já foi pago.
 */
public interface Evento {

    /** Identifica o evento de forma única; é o que permite descartar duplicatas. */
    UUID idDoEvento();

    /** Quando o fato aconteceu no negócio (não quando a mensagem chegou). */
    Instant ocorridoEm();
}
