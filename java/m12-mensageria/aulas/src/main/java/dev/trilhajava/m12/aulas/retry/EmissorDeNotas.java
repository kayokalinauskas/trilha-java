package dev.trilhajava.m12.aulas.retry;

/** A fronteira com o sistema de notas fiscais (em produção, um cliente HTTP). Devolve a chave da nota. */
public interface EmissorDeNotas {

    /** Idempotente por número do pedido: emitir duas vezes o mesmo pedido devolve a mesma nota. */
    String emitir(PedidoPago pedido);
}
