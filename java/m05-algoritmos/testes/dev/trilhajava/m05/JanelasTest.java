package dev.trilhajava.m05;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 05.02 — Janelas deslizantes em métricas de negócio")
class JanelasTest {

    @Test
    @DisplayName("médias móveis de k dias")
    void mediasMoveis() {
        assertThat(Janelas.mediasMoveis(new double[] {10, 20, 30, 40, 50}, 3)).containsExactly(new double[] {20, 30, 40}, within(1e-9));
        assertThat(Janelas.mediasMoveis(new double[] {10, 20}, 3)).isEmpty();
        assertThatThrownBy(() -> Janelas.mediasMoveis(new double[] {1}, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("maior sequência de produtos vistos sem repetir")
    void semRepetir() {
        assertThat(Janelas.maiorSequenciaSemRepetir(new String[] {"a", "b", "c", "a", "b", "b", "d"})).isEqualTo(3);
        assertThat(Janelas.maiorSequenciaSemRepetir(new String[] {"x", "x", "x"})).isEqualTo(1);
        assertThat(Janelas.maiorSequenciaSemRepetir(new String[] {"p", "q", "r", "s"})).isEqualTo(4);
        assertThat(Janelas.maiorSequenciaSemRepetir(new String[] {})).isZero();
    }

    @Test
    @DisplayName("menor número de horas seguidas que atinge a meta de vendas")
    void menorJanela() {
        assertThat(Janelas.menorJanelaQueAtinge(new int[] {2, 3, 1, 2, 4, 3}, 7)).isEqualTo(2);
        assertThat(Janelas.menorJanelaQueAtinge(new int[] {1, 1, 1}, 10)).isZero();
        assertThat(Janelas.menorJanelaQueAtinge(new int[] {10}, 7)).isEqualTo(1);
    }

    @Test
    @DisplayName("maior período de dias seguidos dentro do orçamento")
    void orcamento() {
        assertThat(Janelas.maiorPeriodoNoOrcamento(new int[] {3, 1, 2, 7, 1, 1, 1}, 6)).isEqualTo(3);
        assertThat(Janelas.maiorPeriodoNoOrcamento(new int[] {10, 20}, 5)).isZero();
        assertThat(Janelas.maiorPeriodoNoOrcamento(new int[] {0, 0, 0}, 0)).isEqualTo(3);
    }
}
