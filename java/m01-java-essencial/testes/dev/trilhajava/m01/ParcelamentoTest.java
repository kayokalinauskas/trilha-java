package dev.trilhajava.m01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Exercício 01.02 — Simulador de parcelamento")
class ParcelamentoTest {

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    private static BigDecimal soma(BigDecimal[] parcelas) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal p : parcelas) {
            total = total.add(p);
        }
        return total;
    }

    @Nested
    @DisplayName("sem juros")
    class SemJuros {
        @Test
        @DisplayName("divisão exata gera parcelas iguais")
        void divisaoExata() {
            assertThat(Parcelamento.semJuros(bd("300.00"), 3)).containsExactly(bd("100.00"), bd("100.00"), bd("100.00"));
        }

        @Test
        @DisplayName("centavos que sobram vão para a primeira parcela")
        void sobraNaPrimeira() {
            BigDecimal[] parcelas = Parcelamento.semJuros(bd("100.00"), 3);
            assertThat(parcelas).containsExactly(bd("33.34"), bd("33.33"), bd("33.33"));
            assertThat(soma(parcelas)).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("a soma das parcelas é sempre exatamente o total")
        void somaSempreFecha() {
            for (int n = 1; n <= 24; n++) {
                assertThat(soma(Parcelamento.semJuros(bd("1999.99"), n))).as("em %dx", n).isEqualByComparingTo("1999.99");
            }
        }

        @Test
        @DisplayName("todas as parcelas têm 2 casas decimais")
        void duasCasas() {
            for (BigDecimal p : Parcelamento.semJuros(bd("10"), 3)) {
                assertThat(p.scale()).isEqualTo(2);
            }
        }
    }

    @Nested
    @DisplayName("com juros (Tabela Price)")
    class ComJuros {
        @Test
        @DisplayName("R$ 1.000,00 a 2% a.m. em 12x = R$ 94,56")
        void exemploClassico() {
            assertThat(Parcelamento.parcelaComJuros(bd("1000.00"), bd("0.02"), 12)).isEqualTo(bd("94.56"));
        }

        @Test
        @DisplayName("R$ 2.500,00 a 1,99% a.m. em 10x = R$ 278,17")
        void outroExemplo() {
            assertThat(Parcelamento.parcelaComJuros(bd("2500.00"), bd("0.0199"), 10)).isEqualTo(bd("278.17"));
        }

        @Test
        @DisplayName("taxa zero equivale a dividir igualmente")
        void taxaZero() {
            assertThat(Parcelamento.parcelaComJuros(bd("1200.00"), BigDecimal.ZERO, 12)).isEqualTo(bd("100.00"));
        }

        @Test
        @DisplayName("taxa negativa é recusada")
        void taxaNegativa() {
            assertThatThrownBy(() -> Parcelamento.parcelaComJuros(bd("100"), bd("-0.01"), 2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validações")
    class Validacoes {
        @ParameterizedTest(name = "{0} parcelas é inválido")
        @ValueSource(ints = {0, -1, 25})
        void parcelasForaDoIntervalo(int parcelas) {
            assertThatThrownBy(() -> Parcelamento.semJuros(bd("100"), parcelas))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("parcelas");
        }

        @ParameterizedTest(name = "total {0} é inválido")
        @ValueSource(strings = {"0", "-10", "0.00"})
        void totalNaoPositivo(String total) {
            assertThatThrownBy(() -> Parcelamento.semJuros(bd(total), 2)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("total nulo é inválido")
        void totalNulo() {
            assertThatThrownBy(() -> Parcelamento.semJuros(null, 2)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
