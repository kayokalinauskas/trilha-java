package dev.trilhajava.fintech.dominio;

import java.time.Instant;
import java.util.UUID;

/**
 * Marco 5: algo que aconteceu no domínio e que outras partes do sistema (ou outros sistemas)
 * querem saber. Fatos no passado, imutáveis.
 */
public sealed interface EventoDeDominio permits TransferenciaRealizada {

    /** O agregado de origem: eventos do mesmo agregado são publicados e consumidos em ordem. */
    UUID agregado();

    Instant ocorridoEm();

    /** O nome do evento, estável entre versões (vai no payload e na tabela de outbox). */
    default String tipo() {
        return getClass().getSimpleName();
    }
}
