package dev.trilhajava.m09.pedidos;

import java.math.BigDecimal;

/** Projeção (DTO) preenchida pela própria consulta JPQL com "select new". */
public record FaturamentoDoCliente(String cliente, BigDecimal total) {
}
