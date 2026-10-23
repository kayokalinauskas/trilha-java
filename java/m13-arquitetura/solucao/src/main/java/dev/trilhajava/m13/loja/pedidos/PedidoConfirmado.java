package dev.trilhajava.m13.loja.pedidos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** O evento que o módulo pedidos publica. É o único jeito de os outros módulos saberem do pedido. */
public record PedidoConfirmado(UUID pedido, String cliente, List<Item> itens, BigDecimal total) {
}
