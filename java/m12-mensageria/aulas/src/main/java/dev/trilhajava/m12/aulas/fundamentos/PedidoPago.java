package dev.trilhajava.m12.aulas.fundamentos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Evento: o pedido foi pago. Carrega o estado de que os consumidores precisam. */
public record PedidoPago(UUID idDoEvento, String pedidoId, String clienteId,
                         BigDecimal valor, Instant ocorridoEm) implements Evento {

    public PedidoPago {
        Objects.requireNonNull(idDoEvento, "idDoEvento");
        Objects.requireNonNull(pedidoId, "pedidoId");
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(ocorridoEm, "ocorridoEm");
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("valor precisa ser positivo: " + valor);
        }
    }
}
