package dev.trilhajava.m12.aulas.kafka;

/** As etapas de um pedido, na ordem em que acontecem. A ordem da declaração é a regra de negócio. */
public enum StatusDoPedido {
    CRIADO,
    PAGO,
    ENVIADO,
    ENTREGUE;

    /** A etapa que vem logo depois desta; ENTREGUE é a última. */
    public boolean ehSeguinteDe(StatusDoPedido anterior) {
        return anterior == null ? this == CRIADO : ordinal() == anterior.ordinal() + 1;
    }
}
