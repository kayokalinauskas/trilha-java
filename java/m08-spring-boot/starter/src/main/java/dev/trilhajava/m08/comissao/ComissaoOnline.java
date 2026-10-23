package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;

/**
 * Canal "ONLINE": 3% do valor + R$ 10,00 de bônus se o valor for >= R$ 1.000,00.
 * TODO: implemente RegraDeComissao e transforme esta classe em um bean do Spring.
 */
public class ComissaoOnline {

    public String canal() {
        throw new UnsupportedOperationException("TODO");
    }

    public BigDecimal calcular(Venda venda) {
        throw new UnsupportedOperationException("TODO");
    }
}
