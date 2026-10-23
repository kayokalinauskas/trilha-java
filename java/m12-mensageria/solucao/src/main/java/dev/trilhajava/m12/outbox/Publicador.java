package dev.trilhajava.m12.outbox;

/**
 * Envia um evento ao broker (Kafka, RabbitMQ...) e só retorna depois da confirmação.
 * Qualquer RuntimeException significa "não foi publicado" (ou não se sabe se foi).
 */
public interface Publicador {

    void publicar(EventoDeSaida evento);
}
