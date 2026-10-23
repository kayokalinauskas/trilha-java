package dev.trilhajava.m13.aulas.microsservicos.pedidos;

import java.math.BigDecimal;

/** A porta para o serviço de pagamentos. Na aula, um fake em memória faz esse papel. */
public interface Pagamentos {

    enum Resposta { APROVADO, RECUSADO }

    /** Cobra o pedido. O id do pedido serve de chave de idempotência do lado de lá. */
    Resposta cobrar(String pedidoId, BigDecimal valor);
}
