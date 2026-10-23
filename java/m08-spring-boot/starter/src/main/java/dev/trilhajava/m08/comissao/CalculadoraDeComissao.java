package dev.trilhajava.m08.comissao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TODO: transforme em um bean. O Spring deve injetar TODAS as regras (List<RegraDeComissao>)
 * e a configuração. Dois beans para o mesmo canal → IllegalStateException já no construtor.
 */
public class CalculadoraDeComissao {

    public CalculadoraDeComissao(List<RegraDeComissao> regras, ComissaoProperties properties) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Regra do canal, limitada ao teto, com 2 casas (HALF_UP). Valor negativo → IllegalArgumentException. */
    public BigDecimal calcular(Venda venda) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Total por vendedor, em ordem alfabética. */
    public Map<String, BigDecimal> totalPorVendedor(Collection<Venda> vendas) {
        throw new UnsupportedOperationException("TODO");
    }
}
