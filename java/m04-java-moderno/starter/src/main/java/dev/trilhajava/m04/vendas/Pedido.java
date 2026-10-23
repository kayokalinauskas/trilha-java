package dev.trilhajava.m04.vendas;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Um pedido da loja. Já pronto. */
public record Pedido(String id, String cliente, String categoria, LocalDate data, BigDecimal valor, StatusPedido status) {

    public boolean pago() {
        return status == StatusPedido.PAGO;
    }
}
