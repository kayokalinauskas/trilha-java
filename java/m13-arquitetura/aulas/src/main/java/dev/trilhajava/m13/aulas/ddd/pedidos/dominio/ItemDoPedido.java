package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.util.Objects;

/** Value object dentro do agregado: não tem identidade própria nem repositório. */
public record ItemDoPedido(Sku sku, int quantidade, Dinheiro precoUnitario) {

    public ItemDoPedido {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(precoUnitario, "precoUnitario");
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade precisa ser pelo menos 1: " + quantidade);
        }
    }

    public Dinheiro subtotal() {
        return precoUnitario.vezes(quantidade);
    }

    /** Value objects não mudam: "somar quantidade" devolve um item novo. */
    public ItemDoPedido comMais(int adicional) {
        return new ItemDoPedido(sku, quantidade + adicional, precoUnitario);
    }
}
