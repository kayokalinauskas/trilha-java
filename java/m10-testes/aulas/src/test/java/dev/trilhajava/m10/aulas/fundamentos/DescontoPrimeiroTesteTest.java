package dev.trilhajava.m10.aulas.fundamentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DescontoPrimeiroTesteTest {

    @Test
    @DisplayName("cliente avulso com subtotal de R$ 150,00 ganha 5%: R$ 7,50")
    void avulsoNaFaixaDeCincoPorCento() {
        // Arrange: o cenário
        var calculadora = new CalculadoraDeDesconto();
        var subtotal = new BigDecimal("150.00");

        // Act: a ação que está sendo testada
        BigDecimal desconto = calculadora.calcular(subtotal, Cliente.AVULSO);

        // Assert: o resultado esperado, calculado à mão (150,00 × 5% = 7,50)
        assertThat(desconto).isEqualByComparingTo("7.50");
    }
}
