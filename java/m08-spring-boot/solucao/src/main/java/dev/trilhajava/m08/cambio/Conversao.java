package dev.trilhajava.m08.cambio;

import java.math.BigDecimal;

/** Resultado de converter reais para outra moeda usando a taxa de VENDA. */
public record Conversao(String moeda, BigDecimal valorEmReais, BigDecimal valorConvertido, BigDecimal taxa,
        FonteDaCotacao fonte) {
}
