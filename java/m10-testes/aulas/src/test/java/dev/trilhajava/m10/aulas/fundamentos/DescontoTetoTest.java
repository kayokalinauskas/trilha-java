package dev.trilhajava.m10.aulas.fundamentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Solução do checkpoint: a regra 5, o teto de R$ 50,00, e onde fica a borda dela. */
class DescontoTetoTest {

    CalculadoraDeDesconto calculadora = new CalculadoraDeDesconto();

    @Test
    @DisplayName("assinante com R$ 333,29: 15% dá 49,9935, arredonda para R$ 49,99 (abaixo do teto)")
    void logoAbaixoDoTeto() {
        assertThat(calculadora.calcular(new BigDecimal("333.29"), Cliente.ASSINANTE)).isEqualByComparingTo("49.99");
    }

    @Test
    @DisplayName("assinante com R$ 333,30: 15% dá 49,995, arredonda para R$ 50,00 (encosta no teto)")
    void encostaNoTeto() {
        assertThat(calculadora.calcular(new BigDecimal("333.30"), Cliente.ASSINANTE)).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("assinante com R$ 400,00: 15% daria R$ 60,00, o teto limita a R$ 50,00")
    void assinanteAcimaDoTeto() {
        assertThat(calculadora.calcular(new BigDecimal("400.00"), Cliente.ASSINANTE)).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("avulso com R$ 600,00: 10% daria R$ 60,00, o teto limita a R$ 50,00")
    void avulsoAcimaDoTeto() {
        assertThat(calculadora.calcular(new BigDecimal("600.00"), Cliente.AVULSO)).isEqualByComparingTo("50.00");
    }
}
