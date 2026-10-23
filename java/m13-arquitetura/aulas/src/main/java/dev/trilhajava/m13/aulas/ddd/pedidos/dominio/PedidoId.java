package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.util.Objects;
import java.util.UUID;

/** A identidade do agregado Pedido. Um tipo próprio impede trocar, sem querer, o id de um pedido pelo de um cliente. */
public record PedidoId(UUID valor) {

    public PedidoId {
        Objects.requireNonNull(valor, "valor");
    }

    public static PedidoId novo() {
        return new PedidoId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
