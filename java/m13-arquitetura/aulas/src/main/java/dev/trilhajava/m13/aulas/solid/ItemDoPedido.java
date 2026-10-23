package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.Objects;

/** Uma linha do carrinho: produto, quantidade e preço unitário em reais. */
public record ItemDoPedido(String sku, int quantidade, BigDecimal precoUnitario) {

    public ItemDoPedido {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(precoUnitario, "precoUnitario");
        if (quantidade <= 0) {
            throw new IllegalArgumentException("quantidade deve ser positiva: " + quantidade);
        }
        if (precoUnitario.signum() < 0) {
            throw new IllegalArgumentException("preço unitário negativo: " + precoUnitario);
        }
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
