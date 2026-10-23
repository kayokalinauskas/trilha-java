package dev.trilhajava.m11.aulas.oidc.faturamento;

import java.math.BigDecimal;

/** A visão que o faturamento tem de um pedido (o contrato do lado de quem consome). */
public record PedidoParaFaturar(String numero, String status, BigDecimal total, String chamadoPor) {
}
