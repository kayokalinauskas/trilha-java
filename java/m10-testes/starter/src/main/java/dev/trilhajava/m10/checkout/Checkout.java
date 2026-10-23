package dev.trilhajava.m10.checkout;

public interface Checkout {

    PedidoConfirmado finalizar(Pedido pedido);
}
