package dev.trilhajava.m02.dinheiro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 02.03 — Value object Dinheiro")
class DinheiroTest {

    @Nested
    @DisplayName("criação e igualdade")
    class Criacao {
        @Test
        @DisplayName("normaliza para 2 casas decimais (arredondamento bancário)")
        void normaliza() {
            assertThat(Dinheiro.reais("10").valor()).isEqualTo(new BigDecimal("10.00"));
            assertThat(Dinheiro.reais("10.005").valor()).isEqualTo(new BigDecimal("10.00"));
            assertThat(Dinheiro.reais("10.015").valor()).isEqualTo(new BigDecimal("10.02"));
        }

        @Test
        @DisplayName("10 e 10.00 reais são iguais (e têm o mesmo hashCode)")
        void igualdade() {
            Dinheiro a = Dinheiro.reais("10");
            Dinheiro b = Dinheiro.reais("10.00");
            assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
            Set<Dinheiro> set = new HashSet<>(List.of(a, b));
            assertThat(set).hasSize(1);
        }

        @Test
        @DisplayName("mesma quantia em moedas diferentes não é igual")
        void moedasDiferentes() {
            assertThat(Dinheiro.de("10", Moeda.BRL)).isNotEqualTo(Dinheiro.de("10", Moeda.USD));
        }

        @Test
        @DisplayName("valor e moeda são obrigatórios")
        void obrigatorios() {
            assertThatThrownBy(() -> new Dinheiro(null, Moeda.BRL)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Dinheiro(BigDecimal.ONE, null)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("operações")
    class Operacoes {
        @Test
        @DisplayName("soma e subtrai na mesma moeda, sem alterar os originais")
        void somaESubtrai() {
            Dinheiro dez = Dinheiro.reais("10.00");
            Dinheiro tresECinquenta = Dinheiro.reais("3.50");
            assertThat(dez.somar(tresECinquenta)).isEqualTo(Dinheiro.reais("13.50"));
            assertThat(dez.subtrair(tresECinquenta)).isEqualTo(Dinheiro.reais("6.50"));
            assertThat(dez).isEqualTo(Dinheiro.reais("10.00"));
        }

        @Test
        @DisplayName("somar moedas diferentes é erro")
        void somaMoedasDiferentes() {
            assertThatThrownBy(() -> Dinheiro.reais("1").somar(Dinheiro.de("1", Moeda.EUR)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("BRL")
                    .hasMessageContaining("EUR");
        }

        @Test
        @DisplayName("multiplica por um fator e arredonda")
        void multiplica() {
            assertThat(Dinheiro.reais("19.99").multiplicar(new BigDecimal("3"))).isEqualTo(Dinheiro.reais("59.97"));
            assertThat(Dinheiro.reais("100.00").multiplicar(new BigDecimal("0.075"))).isEqualTo(Dinheiro.reais("7.50"));
        }

        @Test
        @DisplayName("informa se é negativo ou zero")
        void sinais() {
            assertThat(Dinheiro.reais("-0.01").ehNegativo()).isTrue();
            assertThat(Dinheiro.zero(Moeda.BRL).ehZero()).isTrue();
            assertThat(Dinheiro.reais("0.01").ehNegativo()).isFalse();
        }
    }

    @Nested
    @DisplayName("rateio")
    class Rateio {
        @Test
        @DisplayName("divide R$ 100,00 em 3 sem perder centavo")
        void semPerderCentavo() {
            List<Dinheiro> partes = Dinheiro.reais("100.00").ratear(3);
            assertThat(partes).containsExactly(Dinheiro.reais("33.34"), Dinheiro.reais("33.33"), Dinheiro.reais("33.33"));
        }

        @Test
        @DisplayName("divide R$ 0,05 em 3")
        void poucosCentavos() {
            assertThat(Dinheiro.reais("0.05").ratear(3))
                    .containsExactly(Dinheiro.reais("0.02"), Dinheiro.reais("0.02"), Dinheiro.reais("0.01"));
        }

        @Test
        @DisplayName("número de partes precisa ser positivo")
        void partesInvalidas() {
            assertThatThrownBy(() -> Dinheiro.reais("1").ratear(0)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("formatação")
    class Formatacao {
        @Test
        @DisplayName("formata no padrão brasileiro, com símbolo da moeda")
        void formata() {
            assertThat(Dinheiro.reais("1234567.8").formatado()).isEqualTo("R$ 1.234.567,80");
            assertThat(Dinheiro.de("-5", Moeda.USD).formatado()).isEqualTo("-US$ 5,00");
            assertThat(Dinheiro.de("0.5", Moeda.EUR).formatado()).isEqualTo("€ 0,50");
        }
    }
}
