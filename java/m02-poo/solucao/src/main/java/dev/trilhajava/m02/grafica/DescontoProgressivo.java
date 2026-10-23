package dev.trilhajava.m02.grafica;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decorator: embrulha outra Precificacao e aplica desconto acima de um limite de área.
 * Dá para empilhar regras (desconto progressivo + taxa de urgência...) sem mexer nas formas nem no Orcamento.
 */
public final class DescontoProgressivo implements Precificacao {

    private final Precificacao base;
    private final double areaMinimaM2;
    private final BigDecimal fator;

    public DescontoProgressivo(Precificacao base, double areaMinimaM2, BigDecimal percentual) {
        if (base == null) {
            throw new IllegalArgumentException("Precificação base é obrigatória");
        }
        if (percentual == null || percentual.signum() < 0 || percentual.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Percentual deve estar entre 0 e 1 (ex.: 0.10 para 10%)");
        }
        this.base = base;
        this.areaMinimaM2 = areaMinimaM2;
        this.fator = BigDecimal.ONE.subtract(percentual);
    }

    @Override
    public BigDecimal precoPara(double areaTotalM2) {
        BigDecimal preco = base.precoPara(areaTotalM2);
        if (areaTotalM2 <= areaMinimaM2) {
            return preco;
        }
        return preco.multiply(fator).setScale(2, RoundingMode.HALF_EVEN);
    }
}
