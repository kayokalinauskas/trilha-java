package dev.trilhajava.m08.comissao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = {
    "comissao.teto-por-venda=100",
    "comissao.parceiro.percentual=4.5"
})
@DisplayName("Exercício 08.01 — Comissões lidas da configuração")
class ComissaoConfiguradaTest {

    @Autowired
    CalculadoraDeComissao calculadora;

    @Autowired
    ComissaoProperties properties;

    @Test
    @DisplayName("as propriedades comissao.* são lidas e tipadas")
    void propriedades() {
        assertThat(properties.tetoPorVenda()).isEqualByComparingTo("100");
        assertThat(properties.parceiro().percentual()).isEqualByComparingTo("4.5");
    }

    @Test
    @DisplayName("o percentual do parceiro vem da configuração")
    void percentualDoParceiro() {
        assertThat(calculadora.calcular(new Venda("Ana", "PARCEIRO", new BigDecimal("1000.00"))))
                .isEqualByComparingTo("45.00");
    }

    @Test
    @DisplayName("o teto vem da configuração")
    void tetoConfigurado() {
        assertThat(calculadora.calcular(new Venda("Ana", "LOJA", new BigDecimal("5000.00"))))
                .isEqualByComparingTo("100.00");
    }
}
