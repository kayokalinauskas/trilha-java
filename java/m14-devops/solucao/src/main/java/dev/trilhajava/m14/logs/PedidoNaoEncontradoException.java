package dev.trilhajava.m14.logs;

import java.util.UUID;

public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(UUID id) {
        super("pedido não encontrado: " + id);
    }
}
