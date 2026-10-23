package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Vendas por parceiros: percentual configurável (comissao.parceiro.percentual, padrão 2). */
@Component
public class ComissaoParceiro implements RegraDeComissao {

    private final BigDecimal fator;

    // Injeção por construtor: a dependência é obrigatória, explícita e o campo pode ser final.
    public ComissaoParceiro(ComissaoProperties properties) {
        this.fator = properties.parceiro().percentual().movePointLeft(2); // 2 → 0.02
    }

    @Override
    public String canal() {
        return "PARCEIRO";
    }

    @Override
    public BigDecimal calcular(Venda venda) {
        return venda.valor().multiply(fator);
    }
}
