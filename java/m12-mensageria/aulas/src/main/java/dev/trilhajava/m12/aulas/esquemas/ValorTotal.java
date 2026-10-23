package dev.trilhajava.m12.aulas.esquemas;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Valor monetário no evento: quantia decimal exata (no JSON, texto "112.30") e moeda ISO 4217.
 * Dinheiro nunca trafega como número de ponto flutuante.
 */
public record ValorTotal(BigDecimal quantia, String moeda) {

    public ValorTotal {
        Objects.requireNonNull(quantia, "quantia");
        moeda = (moeda == null || moeda.isBlank()) ? "BRL" : moeda;   // default para eventos sem moeda
    }
}
