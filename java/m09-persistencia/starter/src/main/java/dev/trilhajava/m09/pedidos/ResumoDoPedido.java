package dev.trilhajava.m09.pedidos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Uma linha do painel.
 *
 * @param itens    quantidade de linhas (itens) do pedido
 * @param total    soma de quantidade × preço unitário
 * @param produtos nomes dos produtos, em ordem alfabética
 */
public record ResumoDoPedido(long id, String cliente, Instant criadoEm, StatusPedido status, int itens,
        BigDecimal total, List<String> produtos) {
}
