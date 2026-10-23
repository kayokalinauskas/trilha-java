package dev.trilhajava.m10.aulas.fundamentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Partições de equivalência e valores-limite das regras 2, 3 e 4. */
class DescontoCasosTest {

    CalculadoraDeDesconto calculadora = new CalculadoraDeDesconto();

    @ParameterizedTest(name = "{1} com R$ {0} ganha R$ {2}")
    @CsvSource({
        // subtotal, cliente,   desconto   // partição / por quê
        "     0.00, AVULSO,       0.00",   // sem desconto: o menor valor válido
        "    50.00, AVULSO,       0.00",   // sem desconto: um valor típico
        "    99.99, AVULSO,       0.00",   // sem desconto: último centavo da faixa
        "   100.00, AVULSO,       5.00",   // 5%: primeiro centavo da faixa
        "   200.00, AVULSO,      10.00",   // 5%: um valor típico
        "   299.99, AVULSO,      15.00",   // 5%: último centavo (14,9995 arredonda para 15,00)
        "   300.00, AVULSO,      30.00",   // 10%: primeiro centavo da faixa
        "   350.00, AVULSO,      35.00",   // 10%: um valor típico
        "    50.00, ASSINANTE,    2.50",   // assinante sem faixa: só os 5 pontos extras
        "   100.00, ASSINANTE,   10.00",   // assinante na faixa de 5%: 10%
        "   300.00, ASSINANTE,   45.00",   // assinante na faixa de 10%: 15%
        "   100.10, AVULSO,       5.01",   // arredondamento: 5,005 vira 5,01 (HALF_UP)
    })
    void calculaODesconto(BigDecimal subtotal, Cliente cliente, BigDecimal esperado) {
        BigDecimal desconto = calculadora.calcular(subtotal, cliente);

        assertThat(desconto).isEqualByComparingTo(esperado);
    }
}
