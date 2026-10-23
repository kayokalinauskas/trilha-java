package dev.trilhajava.m12.pagamentos;

public class PedidoDesconhecidoException extends RuntimeException {

    public PedidoDesconhecidoException(String pedidoId) {
        super("pedido desconhecido: " + pedidoId);
    }
}
