package dev.trilhajava.m13.aulas.decisoes.frete.legado;

import dev.trilhajava.m13.aulas.decisoes.frete.CalculadoraDeFrete;
import dev.trilhajava.m13.aulas.decisoes.frete.Remessa;
import java.math.BigDecimal;
import java.util.Set;

/** A regra de 2019: preço fixo por região, qualquer que seja o peso. É ela que dá prejuízo. */
public final class CalculadoraDeFreteLegada implements CalculadoraDeFrete {

    private static final Set<String> SUL_E_SUDESTE = Set.of("SP", "RJ", "MG", "ES", "PR", "SC", "RS");

    @Override
    public BigDecimal calcular(Remessa remessa) {
        if (remessa.ufDestino().equals("SP")) {
            return new BigDecimal("15.90");
        }
        if (SUL_E_SUDESTE.contains(remessa.ufDestino())) {
            return new BigDecimal("22.50");
        }
        return new BigDecimal("34.90");
    }
}
