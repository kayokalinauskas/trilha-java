package dev.trilhajava.m10.aulas.fundamentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** O par de testes que teria evitado o bug da promoção "5% a partir de R$ 100". */
class DescontoBordaDosCemReaisTest {

    CalculadoraDeDesconto calculadora = new CalculadoraDeDesconto();

    @Test
    @DisplayName("R$ 99,99 ainda não tem desconto")
    void umCentavoAntesDaFaixa() {
        BigDecimal desconto = calculadora.calcular(new BigDecimal("99.99"), Cliente.AVULSO);

        assertThat(desconto).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("exatamente R$ 100,00 já ganha 5%: R$ 5,00")
    void exatamenteNoInicioDaFaixa() {
        BigDecimal desconto = calculadora.calcular(new BigDecimal("100.00"), Cliente.AVULSO);

        assertThat(desconto).isEqualByComparingTo("5.00");
    }
}
