package dev.trilhajava.m05;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Exercício 05.03 — Pilhas: validação, monotonicidade e avaliação")
class PilhasTest {

    @ParameterizedTest(name = "[{0}] balanceado = {1}")
    @CsvSource(delimiter = '|', textBlock = """
            {"itens": [1, 2, {"x": (3)}]}  | true
            ([)]                            | false
            ((                              | false
            ))                              | false
            sem delimitadores               | true
            """)
    void balanceada(String expressao, boolean esperado) {
        assertThat(Pilhas.balanceada(expressao)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("dias até uma temperatura maior (pilha monotônica)")
    void diasAteMaior() {
        assertThat(Pilhas.diasAteTemperaturaMaior(new int[] {73, 74, 75, 71, 69, 72, 76, 73}))
                .containsExactly(1, 1, 4, 2, 1, 1, 0, 0);
        assertThat(Pilhas.diasAteTemperaturaMaior(new int[] {30, 30, 30})).containsExactly(0, 0, 0);
    }

    @Test
    @DisplayName("avalia expressões em notação polonesa reversa")
    void rpn() {
        assertThat(Pilhas.avaliarRpn("3 4 + 2 *")).isEqualTo(14);
        assertThat(Pilhas.avaliarRpn("10 3 -")).isEqualTo(7);
        assertThat(Pilhas.avaliarRpn("20 6 /")).as("divisão inteira").isEqualTo(3);
        assertThatThrownBy(() -> Pilhas.avaliarRpn("1 +")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Pilhas.avaliarRpn("1 2")).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest(name = "{0} vira {1}")
    @CsvSource({
            "/home/, /home",
            "/a/./b/../../c/, /c",
            "/../, /",
            "//api//v1///pedidos, /api/v1/pedidos"
    })
    void caminho(String entrada, String esperado) {
        assertThat(Pilhas.simplificarCaminho(entrada)).isEqualTo(esperado);
    }
}
