package dev.trilhajava.m10.aulas.reembolso;

import java.math.BigDecimal;

/** Uma linha do pedido: quantas unidades de um produto, a que preço. */
public record Item(String sku, int quantidade, BigDecimal precoUnitario) {

    /** Quantidade × preço unitário. */
    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
