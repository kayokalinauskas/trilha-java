package dev.trilhajava.m13.aulas.solid.legado;

import dev.trilhajava.m13.aulas.solid.Carrinho;
import dev.trilhajava.m13.aulas.solid.ItemDoPedido;
import dev.trilhajava.m13.aulas.solid.TipoDeCliente;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * O cálculo como estava em produção, extraído de um método de 200 linhas que também gravava
 * o pedido e mandava o e-mail. É a referência de comportamento do teste de caracterização:
 * não altere. O código novo não pode depender desta classe.
 */
public class CalculadoraDeDescontosLegada {

    public BigDecimal calcularTotal(Carrinho carrinho) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemDoPedido item : carrinho.itens()) {
            subtotal = subtotal.add(item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade())));
        }
        BigDecimal desconto = BigDecimal.ZERO;
        if (carrinho.tipoDeCliente() == TipoDeCliente.VIP) {
            desconto = desconto.add(subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP));
        }
        if (carrinho.cupom() != null && carrinho.cupom().equalsIgnoreCase("PRIMEIRA10")) {
            if (carrinho.tipoDeCliente() == TipoDeCliente.NOVO) {
                BigDecimal d = subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
                if (d.compareTo(new BigDecimal("50.00")) > 0) {
                    d = new BigDecimal("50.00");
                }
                desconto = desconto.add(d);
            }
        }
        for (ItemDoPedido item : carrinho.itens()) {
            if (item.quantidade() >= 10) {
                BigDecimal totalDoItem = item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade()));
                desconto = desconto.add(totalDoItem.multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP));
            }
        }
        BigDecimal teto = subtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
        if (desconto.compareTo(teto) > 0) {
            desconto = teto;
        }
        return subtotal.subtract(desconto).setScale(2, RoundingMode.HALF_UP);
    }
}
