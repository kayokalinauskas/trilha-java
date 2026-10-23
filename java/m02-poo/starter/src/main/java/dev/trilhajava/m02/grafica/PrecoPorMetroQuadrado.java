package dev.trilhajava.m02.grafica;

import java.math.BigDecimal;

/** Preço fixo por m². Exercício 02.02 — implemente. */
public final class PrecoPorMetroQuadrado implements Precificacao {

    public PrecoPorMetroQuadrado(BigDecimal precoPorM2) {
        throw new UnsupportedOperationException("TODO: preço deve ser > 0");
    }

    @Override
    public BigDecimal precoPara(double areaTotalM2) {
        throw new UnsupportedOperationException("TODO");
    }
}
