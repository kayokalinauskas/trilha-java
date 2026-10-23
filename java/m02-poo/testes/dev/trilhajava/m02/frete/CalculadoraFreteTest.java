package dev.trilhajava.m02.frete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Exercício 02.04 — Frete com sealed types e pattern matching")
class CalculadoraFreteTest {

    /** Segunda-feira. */
    private static final LocalDate HOJE = LocalDate.of(2026, 9, 28);
    private static final Cep PAULISTA = new Cep("01310-100");
    private final CalculadoraFrete calculadora = new CalculadoraFrete();

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Nested
    @DisplayName("CEP")
    class Ceps {
        @Test
        @DisplayName("aceita com ou sem hífen e formata")
        void normaliza() {
            assertThat(new Cep("01310100")).isEqualTo(PAULISTA);
            assertThat(new Cep(" 01310-100 ").formatado()).isEqualTo("01310-100");
        }

        @ParameterizedTest(name = "[{0}] é inválido")
        @ValueSource(strings = {"123", "0131010a", "013101000", ""})
        void invalido(String cep) {
            assertThatThrownBy(() -> new Cep(cep)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("modalidades")
    class Modalidades {
        @Test
        @DisplayName("retirada na loja é grátis")
        void retirada() {
            assertThat(calculadora.calcular(new Retirada("Paulista"), bd("10"), HOJE)).isEqualTo(bd("0.00"));
        }

        @Test
        @DisplayName("padrão: R$ 15 até 1 kg + R$ 2,50 por kg adicional (ou fração)")
        void padrao() {
            assertThat(calculadora.calcular(new EntregaPadrao(PAULISTA, 1.0), bd("100"), HOJE)).isEqualTo(bd("15.00"));
            assertThat(calculadora.calcular(new EntregaPadrao(PAULISTA, 2.3), bd("100"), HOJE)).isEqualTo(bd("20.00"));
            assertThat(calculadora.calcular(new EntregaPadrao(PAULISTA, 0.2), bd("100"), HOJE)).isEqualTo(bd("15.00"));
        }

        @Test
        @DisplayName("padrão é grátis a partir de R$ 199,00 em pedido")
        void padraoGratis() {
            assertThat(calculadora.calcular(new EntregaPadrao(PAULISTA, 5), bd("199.00"), HOJE)).isEqualTo(bd("0.00"));
            assertThat(calculadora.calcular(new EntregaPadrao(PAULISTA, 1), bd("198.99"), HOJE)).isEqualTo(bd("15.00"));
        }

        @Test
        @DisplayName("expressa: padrão x 1,8, nunca grátis, + R$ 20 se for no mesmo dia")
        void expressa() {
            assertThat(calculadora.calcular(new EntregaExpressa(PAULISTA, 2.3, false), bd("500"), HOJE)).isEqualTo(bd("36.00"));
            assertThat(calculadora.calcular(new EntregaExpressa(PAULISTA, 2.3, true), bd("100"), HOJE)).isEqualTo(bd("56.00"));
        }

        @Test
        @DisplayName("agendada: R$ 25 + adicional por kg")
        void agendada() {
            assertThat(calculadora.calcular(new EntregaAgendada(PAULISTA, 1, HOJE.plusDays(2)), bd("50"), HOJE)).isEqualTo(bd("25.00"));
            assertThat(calculadora.calcular(new EntregaAgendada(PAULISTA, 3.5, HOJE.plusDays(3)), bd("50"), HOJE)).isEqualTo(bd("32.50"));
        }

        @Test
        @DisplayName("agendada exige pelo menos 2 dias de antecedência")
        void agendadaAntecedencia() {
            assertThatThrownBy(() -> calculadora.calcular(new EntregaAgendada(PAULISTA, 1, HOJE.plusDays(1)), bd("50"), HOJE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("antecedência");
        }

        @Test
        @DisplayName("agendada não entrega aos domingos")
        void agendadaDomingo() {
            LocalDate domingo = LocalDate.of(2026, 10, 4);
            assertThatThrownBy(() -> calculadora.calcular(new EntregaAgendada(PAULISTA, 1, domingo), bd("50"), HOJE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("domingo");
        }
    }

    @Nested
    @DisplayName("validações")
    class Validacoes {
        @ParameterizedTest(name = "peso {0} kg é inválido")
        @ValueSource(doubles = {0, -1, 30.01})
        void peso(double peso) {
            assertThatThrownBy(() -> new EntregaPadrao(PAULISTA, peso)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("loja de retirada é obrigatória")
        void loja() {
            assertThatThrownBy(() -> new Retirada(" ")).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("valor do pedido não pode ser negativo")
        void valorPedido() {
            assertThatThrownBy(() -> calculadora.calcular(new Retirada("Paulista"), bd("-1"), HOJE))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("descrição")
    class Descricao {
        @Test
        @DisplayName("descreve cada modalidade para o cliente")
        void descreve() {
            assertThat(calculadora.descrever(new Retirada("Paulista"))).isEqualTo("Retirada na loja Paulista");
            assertThat(calculadora.descrever(new EntregaPadrao(PAULISTA, 2))).isEqualTo("Entrega padrão para 01310-100");
            assertThat(calculadora.descrever(new EntregaExpressa(PAULISTA, 2, true))).isEqualTo("Entrega expressa no mesmo dia para 01310-100");
            assertThat(calculadora.descrever(new EntregaExpressa(PAULISTA, 2, false))).isEqualTo("Entrega expressa para 01310-100");
            assertThat(calculadora.descrever(new EntregaAgendada(PAULISTA, 2, LocalDate.of(2026, 10, 1))))
                    .isEqualTo("Entrega agendada para 01/10/2026 em 01310-100");
        }
    }
}
