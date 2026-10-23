package dev.trilhajava.m10.aulas.fundamentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * O contraexemplo da aula: esta suíte executa 100% das linhas da calculadora
 * e mesmo assim deixa passar o bug da promoção. Não use como modelo.
 */
class DescontoSuiteFracaTest {

    CalculadoraDeDesconto calculadora = new CalculadoraDeDesconto();

    @Test
    void compraPequena() {
        assertThat(calculadora.calcular(new BigDecimal("50.00"), Cliente.AVULSO)).isEqualByComparingTo("0.00");
    }

    @Test
    void compraMedia() {
        assertThat(calculadora.calcular(new BigDecimal("200.00"), Cliente.AVULSO)).isEqualByComparingTo("10.00");
    }

    @Test
    void compraGrande() {
        assertThat(calculadora.calcular(new BigDecimal("400.00"), Cliente.AVULSO)).isEqualByComparingTo("40.00");
    }

    @Test
    void assinante() {
        calculadora.calcular(new BigDecimal("1000.00"), Cliente.ASSINANTE);   // executa... e não confere nada
    }

    @Test
    void negativo() {
        assertThatThrownBy(() -> calculadora.calcular(new BigDecimal("-10.00"), Cliente.AVULSO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
