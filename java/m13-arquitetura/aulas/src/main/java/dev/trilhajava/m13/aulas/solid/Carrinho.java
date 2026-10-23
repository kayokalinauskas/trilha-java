package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * O carrinho no momento do fechamento.
 *
 * @param cupom código digitado pela pessoa; {@code null} quando não há cupom
 */
public record Carrinho(String clienteId, TipoDeCliente tipoDeCliente, List<ItemDoPedido> itens, String cupom) {

    public Carrinho {
        Objects.requireNonNull(clienteId, "clienteId");
        Objects.requireNonNull(tipoDeCliente, "tipoDeCliente");
        itens = List.copyOf(itens);
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("carrinho sem itens");
        }
    }

    public BigDecimal subtotal() {
        return itens.stream().map(ItemDoPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Compara o cupom ignorando maiúsculas e minúsculas, como o legado fazia. */
    public boolean temCupom(String codigo) {
        return cupom != null && cupom.equalsIgnoreCase(codigo);
    }
}
