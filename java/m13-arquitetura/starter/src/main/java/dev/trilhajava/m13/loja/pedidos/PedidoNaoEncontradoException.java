package dev.trilhajava.m13.loja.pedidos;

import java.util.UUID;

public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(UUID id) {
        super("pedido não encontrado: " + id);
    }
}
