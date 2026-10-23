package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

/** Uma regra de negócio do pedido foi violada. A mensagem é escrita na linguagem do negócio. */
public class RegraDoPedidoException extends RuntimeException {

    public RegraDoPedidoException(String mensagem) {
        super(mensagem);
    }
}
