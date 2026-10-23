package dev.trilhajava.m13.aulas.decisoes.frete.novo;

import dev.trilhajava.m13.aulas.decisoes.frete.CalculadoraDeFrete;
import dev.trilhajava.m13.aulas.decisoes.frete.Remessa;
import java.math.BigDecimal;
import java.util.Set;

/** A regra nova: a mesma base por região, mais R$ 2,00 por quilo iniciado acima do primeiro. */
public final class CalculadoraDeFretePorPeso implements CalculadoraDeFrete {

    private static final Set<String> SUL_E_SUDESTE = Set.of("SP", "RJ", "MG", "ES", "PR", "SC", "RS");
    private static final BigDecimal ADICIONAL_POR_QUILO = new BigDecimal("2.00");

    @Override
    public BigDecimal calcular(Remessa remessa) {
        int quilosExcedentes = (remessa.pesoEmGramas() - 1) / 1000; // 1 g a 1.000 g: 0; 1.001 g a 2.000 g: 1
        return base(remessa.ufDestino()).add(ADICIONAL_POR_QUILO.multiply(BigDecimal.valueOf(quilosExcedentes)));
    }

    private static BigDecimal base(String uf) {
        if (uf.equals("SP")) {
            return new BigDecimal("15.90");
        }
        return SUL_E_SUDESTE.contains(uf) ? new BigDecimal("22.50") : new BigDecimal("34.90");
    }
}
