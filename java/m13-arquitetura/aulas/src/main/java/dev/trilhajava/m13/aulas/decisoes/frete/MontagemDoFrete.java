package dev.trilhajava.m13.aulas.decisoes.frete;

import dev.trilhajava.m13.aulas.decisoes.frete.legado.CalculadoraDeFreteLegada;
import dev.trilhajava.m13.aulas.decisoes.frete.novo.CalculadoraDeFretePorPeso;

/** O único lugar que conhece as duas implementações (num projeto Spring, seria uma classe @Configuration). */
public final class MontagemDoFrete {

    private MontagemDoFrete() {
    }

    public static CalculadoraDeFrete montar(FeatureFlags flags) {
        return new CalculadoraDeFreteComAlternancia(
                new CalculadoraDeFreteLegada(), new CalculadoraDeFretePorPeso(), flags);
    }
}
