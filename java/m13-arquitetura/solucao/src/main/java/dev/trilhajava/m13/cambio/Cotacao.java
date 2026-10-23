package dev.trilhajava.m13.cambio;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @param obtidaEm      quando o valor veio do provedor
 * @param desatualizada true quando é o último valor conhecido, devolvido porque o provedor falhou
 */
public record Cotacao(String moeda, BigDecimal valor, Instant obtidaEm, boolean desatualizada) {

    Cotacao comoDesatualizada() {
        return new Cotacao(moeda, valor, obtidaEm, true);
    }
}
