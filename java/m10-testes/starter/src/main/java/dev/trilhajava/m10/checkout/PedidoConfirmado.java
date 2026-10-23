package dev.trilhajava.m10.checkout;

import java.math.BigDecimal;
import java.time.Instant;

public record PedidoConfirmado(String numero, String clienteId, BigDecimal total, String codigoAutorizacao,
        Instant confirmadoEm) {
}
