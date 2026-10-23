package dev.trilhajava.m08.cambio;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * O JSON devolvido pela API de cotações:
 * {"moeda": "USD", "compra": 5.1234, "venda": 5.1301, "dataHora": "2026-09-28T13:00:00Z"}
 */
public record Cotacao(String moeda, BigDecimal compra, BigDecimal venda, Instant dataHora) {
}
