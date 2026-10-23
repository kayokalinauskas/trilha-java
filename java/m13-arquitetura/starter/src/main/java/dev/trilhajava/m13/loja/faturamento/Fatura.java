package dev.trilhajava.m13.loja.faturamento;

import java.math.BigDecimal;
import java.util.UUID;

public record Fatura(UUID pedido, String cliente, BigDecimal total, String chaveDeAcesso) {
}
