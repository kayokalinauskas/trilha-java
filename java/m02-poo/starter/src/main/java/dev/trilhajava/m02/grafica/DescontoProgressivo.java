package dev.trilhajava.m02.grafica;

import java.math.BigDecimal;

/**
 * Aplica um desconto percentual sobre OUTRA precificação quando a área passa de um limite.
 * Dica: esta classe não precisa saber como o preço base é calculado. Exercício 02.02 — implemente.
 */
public final class DescontoProgressivo implements Precificacao {

    public DescontoProgressivo(Precificacao base, double areaMinimaM2, BigDecimal percentual) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public BigDecimal precoPara(double areaTotalM2) {
        throw new UnsupportedOperationException("TODO");
    }
}
