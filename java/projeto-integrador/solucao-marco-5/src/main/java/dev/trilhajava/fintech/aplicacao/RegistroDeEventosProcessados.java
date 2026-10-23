package dev.trilhajava.fintech.aplicacao;

import java.util.UUID;

/** Marco 5: a deduplicação dos consumidores de eventos. */
public interface RegistroDeEventosProcessados {

    /**
     * Marca o evento como processado por este consumidor, na transação atual.
     *
     * @return false se ele já tinha sido processado (é uma entrega repetida)
     */
    boolean registrar(String consumidor, UUID evento);
}
