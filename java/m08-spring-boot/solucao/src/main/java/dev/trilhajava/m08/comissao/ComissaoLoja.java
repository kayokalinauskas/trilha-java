package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Vendas na loja física: 5%. */
@Component
public class ComissaoLoja implements RegraDeComissao {

    private static final BigDecimal PERCENTUAL = new BigDecimal("0.05");

    @Override
    public String canal() {
        return "LOJA";
    }

    @Override
    public BigDecimal calcular(Venda venda) {
        return venda.valor().multiply(PERCENTUAL);
    }
}
