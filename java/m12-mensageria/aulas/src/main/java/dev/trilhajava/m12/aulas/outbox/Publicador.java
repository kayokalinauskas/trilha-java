package dev.trilhajava.m12.aulas.outbox;

/**
 * A porta para o broker. O contrato: o método só volta normalmente depois que o broker
 * CONFIRMOU o recebimento. Qualquer {@link RuntimeException} significa "não foi publicado".
 */
public interface Publicador {

    void publicar(Mensagem mensagem);
}
