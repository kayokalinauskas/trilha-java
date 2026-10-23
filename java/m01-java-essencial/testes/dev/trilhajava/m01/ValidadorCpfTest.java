package dev.trilhajava.m01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Exercício 01.01 — Validador de CPF")
class ValidadorCpfTest {

    @Nested
    @DisplayName("somenteDigitos")
    class SomenteDigitos {
        @Test
        @DisplayName("remove pontos, traço e espaços")
        void removeMascara() {
            assertThat(ValidadorCpf.somenteDigitos(" 529.982.247-25 ")).isEqualTo("52998224725");
        }

        @Test
        @DisplayName("null vira texto vazio")
        void nulo() {
            assertThat(ValidadorCpf.somenteDigitos(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("ehValido")
    class EhValido {
        @ParameterizedTest(name = "{0} é válido")
        @ValueSource(strings = {"52998224725", "529.982.247-25", "111.444.777-35", "00000000191"})
        void cpfsValidos(String cpf) {
            assertThat(ValidadorCpf.ehValido(cpf)).isTrue();
        }

        @ParameterizedTest(name = "{0} tem dígito verificador errado")
        @ValueSource(strings = {"52998224724", "529.982.247-26", "11144477734"})
        void digitoErrado(String cpf) {
            assertThat(ValidadorCpf.ehValido(cpf)).isFalse();
        }

        @ParameterizedTest(name = "{0} tem todos os dígitos iguais")
        @ValueSource(strings = {"00000000000", "111.111.111-11", "99999999999"})
        void digitosRepetidos(String cpf) {
            assertThat(ValidadorCpf.ehValido(cpf)).isFalse();
        }

        @ParameterizedTest(name = "[{0}] tem formato inválido")
        @NullAndEmptySource
        @ValueSource(strings = {"123", "529982247250", "5299822472a", "529.982.247-2x"})
        void formatoInvalido(String cpf) {
            assertThat(ValidadorCpf.ehValido(cpf)).isFalse();
        }
    }

    @Nested
    @DisplayName("formatar")
    class Formatar {
        @Test
        @DisplayName("aplica a máscara 000.000.000-00")
        void aplicaMascara() {
            assertThat(ValidadorCpf.formatar("52998224725")).isEqualTo("529.982.247-25");
            assertThat(ValidadorCpf.formatar("529.982.247-25")).isEqualTo("529.982.247-25");
        }

        @Test
        @DisplayName("recusa CPF inválido com mensagem clara")
        void recusaInvalido() {
            assertThatThrownBy(() -> ValidadorCpf.formatar("12345678900"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CPF inválido");
        }
    }
}
