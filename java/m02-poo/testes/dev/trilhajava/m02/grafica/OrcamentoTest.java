package dev.trilhajava.m02.grafica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 02.02 — Orçamento de adesivos da gráfica")
class OrcamentoTest {

    private static final Precificacao QUARENTA_E_CINCO_POR_M2 = new PrecoPorMetroQuadrado(new BigDecimal("45.00"));

    @Nested
    @DisplayName("formas")
    class Formas {
        @Test
        @DisplayName("círculo, retângulo e triângulo calculam a própria área")
        void areasBasicas() {
            assertThat(new Circulo(0.5).area()).isCloseTo(0.7854, within(0.0001));
            assertThat(new Retangulo(2, 1.5).area()).isEqualTo(3.0);
            assertThat(new Triangulo(3, 2).area()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("polígono usa a fórmula do shoelace")
        void poligono() {
            Poligono quadrado = new Poligono(List.of(new Ponto(0, 0), new Ponto(2, 0), new Ponto(2, 2), new Ponto(0, 2)));
            assertThat(quadrado.area()).isEqualTo(4.0);

            Poligono emL = new Poligono(List.of(
                    new Ponto(0, 0), new Ponto(2, 0), new Ponto(2, 1), new Ponto(1, 1), new Ponto(1, 2), new Ponto(0, 2)));
            assertThat(emL.area()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("a ordem dos vértices (horário ou anti-horário) não muda a área")
        void sentido() {
            Poligono horario = new Poligono(List.of(new Ponto(0, 0), new Ponto(0, 3), new Ponto(4, 0)));
            assertThat(horario.area()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("dimensões inválidas são recusadas na criação")
        void dimensoesInvalidas() {
            assertThatThrownBy(() -> new Circulo(0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Retangulo(-1, 2)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Triangulo(2, 0)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("polígono precisa de 3 vértices e área positiva")
        void poligonoInvalido() {
            assertThatThrownBy(() -> new Poligono(List.of(new Ponto(0, 0), new Ponto(1, 1))))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Poligono(List.of(new Ponto(0, 0), new Ponto(1, 1), new Ponto(2, 2))))
                    .as("pontos colineares têm área zero")
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("polígono não é afetado se a lista original mudar depois")
        void copiaDefensiva() {
            java.util.ArrayList<Ponto> vertices = new java.util.ArrayList<>(List.of(new Ponto(0, 0), new Ponto(2, 0), new Ponto(0, 2)));
            Poligono triangulo = new Poligono(vertices);
            vertices.add(new Ponto(2, 2));
            assertThat(triangulo.area()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("toda forma tem uma descrição legível")
        void descricao() {
            assertThat(new Circulo(1).descricao()).containsIgnoringCase("círculo");
            assertThat(new Retangulo(1, 2).descricao()).containsIgnoringCase("retângulo");
        }
    }

    @Nested
    @DisplayName("orçamento")
    class Orcamentos {
        @Test
        @DisplayName("exemplo do enunciado: 2 retângulos 2x1,5 + 3 círculos de raio 0,5")
        void exemploDoEnunciado() {
            Orcamento orcamento = new Orcamento();
            orcamento.adicionar(new Retangulo(2, 1.5), 2);
            orcamento.adicionar(new Circulo(0.5), 3);

            assertThat(orcamento.areaTotal()).isCloseTo(8.3562, within(0.0001));
            assertThat(orcamento.valorTotal(QUARENTA_E_CINCO_POR_M2)).isEqualTo(new BigDecimal("376.03"));
            assertThat(orcamento.itens()).hasSize(2);
            assertThat(orcamento.itens().get(0).areaTotal()).isEqualTo(6.0);
        }

        @Test
        @DisplayName("orçamento vazio custa zero")
        void vazio() {
            Orcamento orcamento = new Orcamento();
            assertThat(orcamento.areaTotal()).isZero();
            assertThat(orcamento.valorTotal(QUARENTA_E_CINCO_POR_M2)).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("quantidade precisa ser pelo menos 1")
        void quantidadeInvalida() {
            Orcamento orcamento = new Orcamento();
            assertThatThrownBy(() -> orcamento.adicionar(new Circulo(1), 0)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> orcamento.adicionar(null, 1)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("itens não podem ser alterados por fora")
        void itensImutaveis() {
            Orcamento orcamento = new Orcamento();
            orcamento.adicionar(new Circulo(1), 1);
            assertThatThrownBy(() -> orcamento.itens().clear()).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("aceita formas novas sem mudar o Orcamento (aberto para extensão)")
        void formaNova() {
            Forma hexagonoDeLado1 = new Forma() {
                @Override
                public double area() {
                    return 3 * Math.sqrt(3) / 2;
                }

                @Override
                public String descricao() {
                    return "Hexágono regular";
                }
            };
            Orcamento orcamento = new Orcamento();
            orcamento.adicionar(hexagonoDeLado1, 2);
            assertThat(orcamento.areaTotal()).isCloseTo(5.196, within(0.001));
        }
    }

    @Nested
    @DisplayName("precificação")
    class Precos {
        @Test
        @DisplayName("desconto progressivo só vale acima do limite de área")
        void descontoProgressivo() {
            Precificacao comDesconto = new DescontoProgressivo(QUARENTA_E_CINCO_POR_M2, 10.0, new BigDecimal("0.10"));
            assertThat(comDesconto.precoPara(12.0)).isEqualTo(new BigDecimal("486.00"));
            assertThat(comDesconto.precoPara(8.0)).isEqualTo(new BigDecimal("360.00"));
            assertThat(comDesconto.precoPara(10.0)).as("exatamente no limite não tem desconto").isEqualTo(new BigDecimal("450.00"));
        }

        @Test
        @DisplayName("preço por m² precisa ser positivo")
        void precoInvalido() {
            assertThatThrownBy(() -> new PrecoPorMetroQuadrado(BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
