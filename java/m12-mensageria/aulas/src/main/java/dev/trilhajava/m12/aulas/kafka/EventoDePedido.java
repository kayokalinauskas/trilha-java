package dev.trilhajava.m12.aulas.kafka;

import java.time.Instant;
import java.util.Objects;

/**
 * Um fato sobre um pedido: "o pedido PED-1001 foi pago às 10h02". Vira JSON no valor da
 * mensagem; o número do pedido também vai na CHAVE, e é ela que decide a partição.
 */
public record EventoDePedido(String numeroPedido, StatusDoPedido status, Instant ocorridoEm) {

    public EventoDePedido {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(ocorridoEm, "ocorridoEm");
        if (numeroPedido == null || numeroPedido.isBlank()) {
            throw new IllegalArgumentException("o número do pedido é obrigatório");
        }
    }
}
