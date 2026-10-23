package dev.trilhajava.m13.aulas.solid;

import java.time.Instant;

/** O pedido depois do fechamento, como fica gravado. */
public record PedidoFechado(String numero, String clienteId, Orcamento orcamento, Instant fechadoEm) {
}
