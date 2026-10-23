package dev.trilhajava.m10.aulas.reembolso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Um pedido já pago e entregue.
 *
 * @param autorizacaoPagamento código que o gateway devolveu na cobrança; é com ele que se estorna
 * @param entregueEm           data em que o cliente recebeu o pedido (início do prazo de arrependimento)
 */
public record Pedido(String numero, String cliente, String autorizacaoPagamento, List<Item> itens,
        LocalDate entregueEm) {

    public Pedido {
        itens = List.copyOf(itens);
    }

    /** Soma dos subtotais dos itens. */
    public BigDecimal total() {
        return itens.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
