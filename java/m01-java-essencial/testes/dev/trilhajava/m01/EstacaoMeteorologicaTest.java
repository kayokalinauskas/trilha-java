package dev.trilhajava.m01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 01.03 — Relatório da estação meteorológica")
class EstacaoMeteorologicaTest {

    private final double[] semana = {21.5, 23.0, 24.1, 22.8, 25.3, 26.0, 26.7};

    @Test
    @DisplayName("encontra mínima e máxima")
    void minimaEMaxima() {
        assertThat(EstacaoMeteorologica.minima(semana)).isEqualTo(21.5);
        assertThat(EstacaoMeteorologica.maxima(semana)).isEqualTo(26.7);
    }

    @Test
    @DisplayName("calcula a média")
    void media() {
        assertThat(EstacaoMeteorologica.media(semana)).isCloseTo(24.2, within(0.001));
    }

    @Test
    @DisplayName("maior sequência de leituras em alta estrita")
    void maiorSequenciaDeAlta() {
        // 22.8 -> 25.3 -> 26.0 -> 26.7 são 4 leituras subindo
        assertThat(EstacaoMeteorologica.maiorSequenciaDeAlta(semana)).isEqualTo(4);
        assertThat(EstacaoMeteorologica.maiorSequenciaDeAlta(new double[] {30, 29, 28})).isEqualTo(1);
        assertThat(EstacaoMeteorologica.maiorSequenciaDeAlta(new double[] {20, 20, 21})).isEqualTo(2);
    }

    @Test
    @DisplayName("conta leituras acima de um limite (exclusivo)")
    void acimaDe() {
        assertThat(EstacaoMeteorologica.contarAcimaDe(semana, 25.3)).isEqualTo(2);
        assertThat(EstacaoMeteorologica.contarAcimaDe(semana, 100)).isZero();
    }

    @Test
    @DisplayName("funciona com uma única leitura")
    void umaLeitura() {
        double[] uma = {18.0};
        assertThat(EstacaoMeteorologica.minima(uma)).isEqualTo(18.0);
        assertThat(EstacaoMeteorologica.maxima(uma)).isEqualTo(18.0);
        assertThat(EstacaoMeteorologica.media(uma)).isEqualTo(18.0);
        assertThat(EstacaoMeteorologica.maiorSequenciaDeAlta(uma)).isEqualTo(1);
    }

    @Test
    @DisplayName("funciona com temperaturas negativas")
    void negativas() {
        double[] inverno = {-3.5, -1.0, -7.2};
        assertThat(EstacaoMeteorologica.minima(inverno)).isEqualTo(-7.2);
        assertThat(EstacaoMeteorologica.maxima(inverno)).isEqualTo(-1.0);
    }

    @Test
    @DisplayName("não altera o array recebido")
    void naoAlteraEntrada() {
        double[] copia = semana.clone();
        EstacaoMeteorologica.minima(semana);
        EstacaoMeteorologica.maiorSequenciaDeAlta(semana);
        EstacaoMeteorologica.formatarRelatorio(semana);
        assertThat(semana).containsExactly(copia);
    }

    @Test
    @DisplayName("array vazio ou nulo é recusado")
    void vazio() {
        assertThatThrownBy(() -> EstacaoMeteorologica.media(new double[0])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EstacaoMeteorologica.minima(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("gera o relatório formatado com uma casa decimal")
    void relatorio() {
        String esperado = """
                Leituras: 7
                Mínima: 21.5 °C
                Máxima: 26.7 °C
                Média: 24.2 °C
                Maior sequência de alta: 4 leituras""";
        assertThat(EstacaoMeteorologica.formatarRelatorio(semana)).isEqualTo(esperado);
    }
}
