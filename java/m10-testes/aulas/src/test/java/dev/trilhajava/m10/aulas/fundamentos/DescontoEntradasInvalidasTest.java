package dev.trilhajava.m10.aulas.fundamentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regra 1: os casos de erro também são comportamento. */
class DescontoEntradasInvalidasTest {

    CalculadoraDeDesconto calculadora = new CalculadoraDeDesconto();

    @Test
    @DisplayName("subtotal negativo é recusado, com o valor na mensagem")
    void subtotalNegativo() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> calculadora.calcular(new BigDecimal("-0.01"), Cliente.AVULSO))
                .withMessageContaining("-0.01");
    }

    @Test
    @DisplayName("subtotal zero é válido e não tem desconto")
    void subtotalZero() {
        assertThat(calculadora.calcular(BigDecimal.ZERO, Cliente.AVULSO)).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("subtotal e cliente são obrigatórios")
    void camposObrigatorios() {
        assertThatNullPointerException()
                .isThrownBy(() -> calculadora.calcular(null, Cliente.AVULSO))
                .withMessage("subtotal");
        assertThatNullPointerException()
                .isThrownBy(() -> calculadora.calcular(new BigDecimal("10.00"), null))
                .withMessage("cliente");
    }
}
