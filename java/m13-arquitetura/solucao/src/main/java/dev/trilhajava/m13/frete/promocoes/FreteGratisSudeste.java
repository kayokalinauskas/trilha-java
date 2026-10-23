package dev.trilhajava.m13.frete.promocoes;

import dev.trilhajava.m13.frete.Cotacao;
import dev.trilhajava.m13.frete.Pedido;
import dev.trilhajava.m13.frete.RegraDePromocao;
import dev.trilhajava.m13.frete.Regiao;
import java.math.BigDecimal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** PAC grátis no Sudeste a partir de R$ 299,00 em itens (inclusive o seguro). */
@Component
@Order(2)
public class FreteGratisSudeste implements RegraDePromocao {

    private static final BigDecimal MINIMO = new BigDecimal("299.00");

    @Override
    public Cotacao aplicar(Pedido pedido, Cotacao cotacao) {
        boolean elegivel = "PAC".equals(cotacao.modalidade())
                && pedido.regiao() == Regiao.SUDESTE
                && pedido.valorDosItens().compareTo(MINIMO) >= 0;
        return elegivel ? cotacao.comValor(BigDecimal.ZERO) : cotacao;
    }
}
