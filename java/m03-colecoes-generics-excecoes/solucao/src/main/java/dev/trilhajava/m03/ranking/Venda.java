package dev.trilhajava.m03.ranking;

import java.math.BigDecimal;

public record Venda(String produto, int quantidade, BigDecimal valorUnitario) {

    public Venda {
        if (produto == null || produto.isBlank()) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (quantidade < 1) {
            throw new IllegalArgumentException("Quantidade deve ser pelo menos 1");
        }
        if (valorUnitario == null || valorUnitario.signum() < 0) {
            throw new IllegalArgumentException("Valor unitário deve ser zero ou positivo");
        }
        produto = produto.strip();
    }

    public BigDecimal total() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
