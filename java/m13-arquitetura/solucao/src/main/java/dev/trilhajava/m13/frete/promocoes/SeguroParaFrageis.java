package dev.trilhajava.m13.frete.promocoes;

import dev.trilhajava.m13.frete.Cotacao;
import dev.trilhajava.m13.frete.Pedido;
import dev.trilhajava.m13.frete.RegraDePromocao;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Item frágil: + R$ 8,00 de seguro no PAC e no SEDEX. Vem ANTES do frete grátis. */
@Component
@Order(1)
public class SeguroParaFrageis implements RegraDePromocao {

    private static final BigDecimal SEGURO = new BigDecimal("8.00");
    private static final Set<String> MODALIDADES = Set.of("PAC", "SEDEX");

    @Override
    public Cotacao aplicar(Pedido pedido, Cotacao cotacao) {
        if (!pedido.temItemFragil() || !MODALIDADES.contains(cotacao.modalidade())) {
            return cotacao;
        }
        return cotacao.comValor(cotacao.valor().add(SEGURO));
    }
}
