package dev.trilhajava.m13.aulas.padroes;

import java.math.BigDecimal;

/** Evento: algo que JÁ aconteceu, no passado, com os dados de que os ouvintes precisam. */
public record PagamentoAprovado(String pedidoId, String clienteId, BigDecimal valor, String codigoDeAutorizacao) {
}
