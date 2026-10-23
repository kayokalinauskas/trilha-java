package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Vendas online: 3%, mais um bônus fixo de R$ 10,00 para vendas a partir de R$ 1.000,00. */
@Component
public class ComissaoOnline implements RegraDeComissao {

    private static final BigDecimal PERCENTUAL = new BigDecimal("0.03");
    private static final BigDecimal VALOR_PARA_BONUS = new BigDecimal("1000.00");
    private static final BigDecimal BONUS = new BigDecimal("10.00");

    @Override
    public String canal() {
        return "ONLINE";
    }

    @Override
    public BigDecimal calcular(Venda venda) {
        BigDecimal comissao = venda.valor().multiply(PERCENTUAL);
        return venda.valor().compareTo(VALOR_PARA_BONUS) >= 0 ? comissao.add(BONUS) : comissao;
    }
}
