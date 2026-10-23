package dev.trilhajava.m10.frete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Suíte de referência do exercício 10.01. Cada linha da tabela isola uma regra (ou a borda dela),
 * e os valores esperados foram calculados à mão, fora do código: nunca copiados da implementação.
 */
@DisplayName("Calculadora de frete")
class CalculadoraDeFreteTest {

    private final CalculadoraDeFrete calculadora = new CalculadoraDeFrete();

    private BigDecimal frete(String origem, String destino, String peso, String valor, Modalidade modalidade) {
        return calculadora.calcular(new Envio(origem, destino, new BigDecimal(peso), new BigDecimal(valor), modalidade));
    }

    @ParameterizedTest(name = "{0}: R$ {6}", quoteTextArguments = false)
    @CsvSource(delimiter = '|', textBlock = """
        # regra (o que a linha isola)                          | origem | destino | peso | valor | modalidade | esperado
        mesma UF: base 12,00 + ICMS 18%                        | SP     | SP      | 1    | 0     | ECONOMICA  | 14.16
        mesma região: base 18,00 + ICMS 12%                    | SP     | RJ      | 1    | 0     | ECONOMICA  | 20.16
        regiões diferentes: base 25,00 + ICMS 12%              | SP     | BA      | 1    | 0     | ECONOMICA  | 28.00
        até 1 kg e seguro na franquia: nada a mais             | RS     | SC      | 0.5  | 100   | ECONOMICA  | 20.16
        1,2 kg: a fração conta como 1 kg adicional             | SP     | SP      | 1.2  | 0     | ECONOMICA  | 17.11
        2 kg: só o kg adicional é cobrado                      | SP     | SP      | 2    | 0     | ECONOMICA  | 17.11
        30 kg, o máximo: 29 kg adicionais                      | SP     | SP      | 30   | 0     | ECONOMICA  | 99.71
        EXPRESSA: +60% sobre base + peso                       | SP     | RJ      | 3    | 0     | EXPRESSA   | 41.22
        seguro: 1% só do que passa de R$ 100                   | SP     | BA      | 1    | 250   | ECONOMICA  | 29.68
        arredondamento HALF_UP: 20,6752 vira 20,68             | SP     | RJ      | 1    | 146   | ECONOMICA  | 20.68
        frete grátis: ECONOMICA, R$ 500 exatos, mesma região   | SP     | RJ      | 1    | 500   | ECONOMICA  | 0.00
        EXPRESSA nunca é grátis                                | SP     | RJ      | 1    | 500   | EXPRESSA   | 36.74
        regiões diferentes nunca são grátis                    | SP     | BA      | 1    | 500   | ECONOMICA  | 32.48
        tudo junto: EXPRESSA, peso fracionado e seguro         | AM     | PA      | 5.5  | 1000  | EXPRESSA   | 64.74
        """)
    @DisplayName("calcula o frete conforme as regras")
    void calcula(String regra, String origem, String destino, BigDecimal peso, BigDecimal valor,
            Modalidade modalidade, BigDecimal esperado) {
        BigDecimal resultado = calculadora.calcular(new Envio(origem, destino, peso, valor, modalidade));

        assertThat(resultado).as(regra).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("o resultado tem sempre 2 casas decimais, inclusive o frete grátis")
    void duasCasas() {
        assertThat(frete("SP", "RJ", "1", "500", Modalidade.ECONOMICA)).isEqualTo(new BigDecimal("0.00"));
        assertThat(frete("SP", "SP", "1", "0", Modalidade.ECONOMICA).scale()).isEqualTo(2);
    }

    @Nested
    @DisplayName("entradas inválidas: IllegalArgumentException")
    class Invalidas {

        @ParameterizedTest(name = "peso {0} kg", quoteTextArguments = false)
        @ValueSource(strings = {"0", "-1", "30.01", "31"})
        @DisplayName("peso fora de (0, 30]")
        void peso(String peso) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> frete("SP", "SP", peso, "0", Modalidade.ECONOMICA));
        }

        @Test
        @DisplayName("valor declarado negativo")
        void valorNegativo() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> frete("SP", "SP", "1", "-0.01", Modalidade.ECONOMICA));
        }

        @Test
        @DisplayName("UF inexistente, na origem ou no destino")
        void ufInexistente() {
            assertThatIllegalArgumentException().isThrownBy(() -> frete("XX", "SP", "1", "0", Modalidade.ECONOMICA));
            assertThatIllegalArgumentException().isThrownBy(() -> frete("SP", "XX", "1", "0", Modalidade.ECONOMICA));
        }

        @Test
        @DisplayName("modalidade ausente")
        void semModalidade() {
            assertThatIllegalArgumentException().isThrownBy(() -> frete("SP", "SP", "1", "0", null));
        }
    }
}
