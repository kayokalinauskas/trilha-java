package dev.trilhajava.m03.ranking;

import java.math.BigDecimal;

public record Venda(String produto, int quantidade, BigDecimal valorUnitario) {

    public Venda {
        // TODO: produto não pode ser nulo/em branco; quantidade >= 1; valorUnitario não nulo e >= 0.
    }

    public BigDecimal total() {
        throw new UnsupportedOperationException("TODO");
    }
}
