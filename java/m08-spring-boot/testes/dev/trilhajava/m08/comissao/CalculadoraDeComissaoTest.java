package dev.trilhajava.m08.comissao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DisplayName("Exercício 08.01 — Motor de comissões com injeção de dependências")
class CalculadoraDeComissaoTest {

    /** Um canal novo, criado só no teste: a calculadora precisa aceitá-lo sem nenhuma alteração. */
    @TestConfiguration
    static class CanalNovo {
        @Bean
        RegraDeComissao marketplace() {
            return new RegraDeComissao() {
                @Override
                public String canal() {
                    return "MARKETPLACE";
                }

                @Override
                public BigDecimal calcular(Venda venda) {
                    return venda.valor().multiply(new BigDecimal("0.01"));
                }
            };
        }
    }

    @Autowired
    CalculadoraDeComissao calculadora;

    @Autowired
    List<RegraDeComissao> regras;

    @Autowired
    ComissaoProperties properties;

    private static Venda venda(String vendedor, String canal, String valor) {
        return new Venda(vendedor, canal, new BigDecimal(valor));
    }

    @Test
    @DisplayName("as regras LOJA, ONLINE e PARCEIRO são beans descobertos pelo Spring")
    void regrasSaoBeans() {
        assertThat(regras).extracting(RegraDeComissao::canal).contains("LOJA", "ONLINE", "PARCEIRO", "MARKETPLACE");
    }

    @ParameterizedTest(name = "{0} de R$ {1} → R$ {2}")
    @CsvSource({
        "LOJA,     200.00,  10.00",
        "LOJA,      19.99,   1.00",
        "ONLINE,   999.99,  30.00",
        "ONLINE,  1000.00,  40.00",
        "ONLINE,     0.00,   0.00",
        "PARCEIRO, 150.00,   3.00",
    })
    @DisplayName("calcula a comissão de cada canal com 2 casas decimais")
    void calculaPorCanal(String canal, String valor, String esperado) {
        BigDecimal comissao = calculadora.calcular(venda("Ana", canal, valor));
        assertThat(comissao).isEqualByComparingTo(esperado);
        assertThat(comissao.scale()).as("escala").isEqualTo(2);
    }

    @Test
    @DisplayName("um canal novo funciona só por existir como bean (aberto para extensão)")
    void canalNovo() {
        assertThat(calculadora.calcular(venda("Ana", "MARKETPLACE", "1000.00"))).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("a comissão de uma venda nunca passa do teto (padrão R$ 500,00)")
    void teto() {
        assertThat(properties.tetoPorVenda()).isEqualByComparingTo("500.00");
        assertThat(calculadora.calcular(venda("Ana", "LOJA", "20000.00"))).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("canal sem regra → CanalDesconhecidoException")
    void canalDesconhecido() {
        assertThatThrownBy(() -> calculadora.calcular(venda("Ana", "CRIPTO", "10")))
                .isInstanceOf(CanalDesconhecidoException.class)
                .hasMessageContaining("CRIPTO");
    }

    @Test
    @DisplayName("valor negativo → IllegalArgumentException")
    void valorNegativo() {
        assertThatThrownBy(() -> calculadora.calcular(venda("Ana", "LOJA", "-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("soma as comissões por vendedor, em ordem alfabética")
    void totalPorVendedor() {
        var vendas = List.of(
                venda("Bruno", "ONLINE", "1000.00"),
                venda("Ana", "LOJA", "200.00"),
                venda("Ana", "PARCEIRO", "150.00"));
        assertThat(calculadora.totalPorVendedor(vendas))
                .containsExactly(entry("Ana", new BigDecimal("13.00")), entry("Bruno", new BigDecimal("40.00")));
    }

    @Test
    @DisplayName("duas regras para o mesmo canal → IllegalStateException já na construção")
    void canalDuplicado() {
        RegraDeComissao loja = regras.stream().filter(r -> r.canal().equals("LOJA")).findFirst().orElseThrow();
        assertThatThrownBy(() -> new CalculadoraDeComissao(List.of(loja, loja), properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LOJA");
    }
}
