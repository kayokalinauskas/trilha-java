package dev.trilhajava.m12.aulas.retry;

import java.math.BigDecimal;

/** O evento que chega em "pedidos.pagos": o pagamento foi aprovado e a nota fiscal precisa ser emitida. */
public record PedidoPago(String numeroPedido, String clienteId, BigDecimal valor) {
}
