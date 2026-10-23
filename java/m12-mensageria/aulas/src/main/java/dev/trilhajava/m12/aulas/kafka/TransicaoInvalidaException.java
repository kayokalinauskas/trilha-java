package dev.trilhajava.m12.aulas.kafka;

/** Um evento chegou fora de ordem: a etapa anterior do pedido ainda não foi vista. */
public class TransicaoInvalidaException extends RuntimeException {

    public TransicaoInvalidaException(String numeroPedido, StatusDoPedido atual, StatusDoPedido recebido) {
        super("pedido %s: recebido %s, mas o status atual é %s".formatted(
                numeroPedido, recebido, atual == null ? "(nenhum)" : atual));
    }
}
