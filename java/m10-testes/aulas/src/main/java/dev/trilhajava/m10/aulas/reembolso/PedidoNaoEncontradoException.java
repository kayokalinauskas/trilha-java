package dev.trilhajava.m10.aulas.reembolso;

/** Não existe pedido com o número informado. */
public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
