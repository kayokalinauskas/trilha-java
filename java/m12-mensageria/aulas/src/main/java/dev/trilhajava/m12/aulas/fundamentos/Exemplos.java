package dev.trilhajava.m12.aulas.fundamentos;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/** Dados de exemplo das demonstrações, com ids determinísticos para a saída ser sempre a mesma. */
public final class Exemplos {

    public static final Instant PAGO_EM = Instant.parse("2026-10-05T14:30:00Z");

    private Exemplos() {
    }

    public static PedidoPago pedidoPago(String pedidoId, String clienteId, String valor) {
        UUID id = UUID.nameUUIDFromBytes(("PedidoPago:" + pedidoId).getBytes(StandardCharsets.UTF_8));
        return new PedidoPago(id, pedidoId, clienteId, new BigDecimal(valor), PAGO_EM);
    }
}
