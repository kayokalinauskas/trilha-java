package dev.trilhajava.m13.aulas.solid;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m13.aulas.solid.legado.CalculadoraDeDescontosLegada;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Golden master: o código novo dá exatamente o mesmo total que o legado, em 2.000 carrinhos. */
class CaracterizacaoDosDescontosTest {

    private final CalculadoraDeDescontosLegada legado = new CalculadoraDeDescontosLegada();
    private final CalculadoraDeDescontos nova = new CalculadoraDeDescontos(
            List.of(new DescontoVip(), new DescontoPrimeiraCompra(), new DescontoAtacado()));

    @Test
    void mesmoTotalQueOLegadoEmDoisMilCarrinhos() {
        var aleatorio = new Random(2026);   // semente fixa: a mesma sequência em toda execução
        for (int i = 0; i < 2_000; i++) {
            Carrinho carrinho = GeradorDeCarrinhos.aleatorio(aleatorio);
            assertThat(nova.orcar(carrinho).total())
                    .as("carrinho nº %d: %s", i, carrinho)
                    .isEqualTo(legado.calcularTotal(carrinho));
        }
    }
}
