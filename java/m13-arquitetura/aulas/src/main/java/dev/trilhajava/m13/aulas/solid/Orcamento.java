package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.List;

/**
 * O resultado do cálculo: o que a pessoa vê antes de pagar.
 *
 * @param descontos tudo o que as regras concederam, antes do teto
 * @param descontoAplicado a soma dos descontos, limitada ao teto
 */
public record Orcamento(BigDecimal subtotal, List<Desconto> descontos, BigDecimal descontoAplicado, BigDecimal total) {

    public Orcamento {
        descontos = List.copyOf(descontos);
    }
}
