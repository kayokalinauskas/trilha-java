package dev.trilhajava.m13.aulas.decisoes.pullrequest;

import dev.trilhajava.m13.aulas.decisoes.frete.Remessa;
import dev.trilhajava.m13.aulas.decisoes.frete.legado.CalculadoraDeFreteLegada;
import java.math.BigDecimal;

/** Um pull request "rápido" que fura a abstração: usa a regra antiga direto. A fitness function pega. */
public class RelatorioDeFreteApressado {

    public BigDecimal freteMedioParaSp() {
        return new CalculadoraDeFreteLegada().calcular(new Remessa("relatorio", "SP", 1_000));
    }
}
