package dev.trilhajava.m13.aulas.decisoes.frete;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m13.aulas.decisoes.frete.legado.CalculadoraDeFreteLegada;
import dev.trilhajava.m13.aulas.decisoes.frete.novo.CalculadoraDeFretePorPeso;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class AlternanciaDeFreteTest {

    static final Remessa CINCO_QUILOS_PARA_SP = new Remessa("cliente-42", "SP", 5_000);

    final FlagsEmMemoria flags = new FlagsEmMemoria();
    final CalculadoraDeFrete frete = MontagemDoFrete.montar(flags);

    @Test
    void aFlagDecideQualRegraVale() {
        assertThat(frete.calcular(CINCO_QUILOS_PARA_SP)).isEqualByComparingTo("15.90"); // flag ausente: legada

        flags.definir(CalculadoraDeFreteComAlternancia.FLAG, 100);
        assertThat(frete.calcular(CINCO_QUILOS_PARA_SP)).isEqualByComparingTo("23.90"); // 15,90 + 4 × 2,00

        flags.definir(CalculadoraDeFreteComAlternancia.FLAG, 0); // o "botão de pânico": volta na hora, sem deploy
        assertThat(frete.calcular(CINCO_QUILOS_PARA_SP)).isEqualByComparingTo("15.90");
    }

    @Test
    void oRolloutGradualAtingeCercaDoPercentualESempreOsMesmosClientes() {
        flags.definir(CalculadoraDeFreteComAlternancia.FLAG, 30);
        var alternancia = (CalculadoraDeFreteComAlternancia) frete;

        long naNova = IntStream.range(0, 10_000)
                .filter(i -> alternancia.usaNova("cliente-" + i))
                .count();
        assertThat(naNova).isBetween(2_700L, 3_300L);

        var umCliente = new Remessa("cliente-7", "BA", 2_500);
        BigDecimal primeira = frete.calcular(umCliente);
        IntStream.range(0, 100).forEach(i -> assertThat(frete.calcular(umCliente)).isEqualByComparingTo(primeira));
    }

    @Test
    void oModoSombraDevolveSempreOOficialERegistraAsDiferencas() {
        var divergencias = new ArrayList<CalculadoraDeFreteEmSombra.Divergencia>();
        CalculadoraDeFrete quebrada = remessa -> {
            throw new IllegalStateException("tabela de peso não carregada");
        };
        var sombra = new CalculadoraDeFreteEmSombra(
                new CalculadoraDeFreteLegada(), new CalculadoraDeFretePorPeso(), divergencias::add);
        var sombraQuebrada = new CalculadoraDeFreteEmSombra(
                new CalculadoraDeFreteLegada(), quebrada, divergencias::add);

        assertThat(sombra.calcular(new Remessa("cliente-1", "SP", 800))).isEqualByComparingTo("15.90");
        assertThat(sombra.calcular(CINCO_QUILOS_PARA_SP)).isEqualByComparingTo("15.90");
        assertThat(sombraQuebrada.calcular(CINCO_QUILOS_PARA_SP)).isEqualByComparingTo("15.90");

        assertThat(divergencias).hasSize(2);
        assertThat(divergencias.get(0).candidata()).isEqualByComparingTo("23.90");
        assertThat(divergencias.get(1).erro()).contains("tabela de peso não carregada");
    }
}
