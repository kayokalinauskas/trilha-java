package dev.trilhajava.m13.loja.pedidos;

import java.math.BigDecimal;

/** Parte da API pública do módulo pedidos: viaja dentro do evento PedidoConfirmado. */
public record Item(String sku, int quantidade, BigDecimal precoUnitario) {

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
