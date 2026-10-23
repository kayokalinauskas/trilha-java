package dev.trilhajava.m14.saude;

import java.time.Duration;

/**
 * O provedor externo de pagamentos. {@link #ping()} devolve a latência medida
 * ou lança exceção se o provedor não responder. Nos testes, um mock.
 */
public interface ProvedorDePagamentos {

    Duration ping();
}
