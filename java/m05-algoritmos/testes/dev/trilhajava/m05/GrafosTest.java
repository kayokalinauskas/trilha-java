package dev.trilhajava.m05;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 05.05 — Grafos: rotas de entrega e ordem de build")
class GrafosTest {

    @Nested
    @DisplayName("RedeDeEntregas (BFS)")
    class Rede {
        //  SP — Campinas — Ribeirão
        //  |                  |
        //  Santos         Franca        BH (isolado com Contagem)
        private final RedeDeEntregas rede = new RedeDeEntregas(List.of(
                List.of("SP", "Campinas"), List.of("Campinas", "Ribeirão"), List.of("SP", "Santos"),
                List.of("Ribeirão", "Franca"), List.of("BH", "Contagem")));

        @Test
        @DisplayName("menor número de trechos entre dois centros")
        void distancia() {
            assertThat(rede.menorNumeroDeTrechos("Santos", "Franca")).isEqualTo(4);
            assertThat(rede.menorNumeroDeTrechos("SP", "SP")).isZero();
            assertThat(rede.menorNumeroDeTrechos("SP", "BH")).isEqualTo(-1);
            assertThat(rede.menorNumeroDeTrechos("SP", "Marte")).isEqualTo(-1);
        }

        @Test
        @DisplayName("caminho mais curto, do início ao fim")
        void caminho() {
            assertThat(rede.caminhoMaisCurto("Santos", "Ribeirão")).containsExactly("Santos", "SP", "Campinas", "Ribeirão");
            assertThat(rede.caminhoMaisCurto("SP", "Contagem")).isEmpty();
        }

        @Test
        @DisplayName("quantidade de regiões desconectadas")
        void regioes() {
            assertThat(rede.regioesDesconectadas()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("OrdemDeBuild (ordenação topológica)")
    class Build {
        @Test
        @DisplayName("dependências vêm antes; empates em ordem alfabética")
        void ordem() {
            Map<String, List<String>> deps = Map.of(
                    "app", List.of("dominio", "infra"),
                    "infra", List.of("dominio"),
                    "dominio", List.of(),
                    "cli", List.of("app"),
                    "docs", List.of());
            assertThat(OrdemDeBuild.ordenar(deps)).containsExactly("docs", "dominio", "infra", "app", "cli");
        }

        @Test
        @DisplayName("dependência citada mas não declarada também entra")
        void implicita() {
            assertThat(OrdemDeBuild.ordenar(Map.of("app", List.of("lib")))).containsExactly("lib", "app");
        }

        @Test
        @DisplayName("ciclo é detectado")
        void ciclo() {
            Map<String, List<String>> deps = Map.of("a", List.of("b"), "b", List.of("c"), "c", List.of("a"), "d", List.of());
            assertThatThrownBy(() -> OrdemDeBuild.ordenar(deps))
                    .isInstanceOf(CicloDeDependenciaException.class)
                    .hasMessageContaining("a").hasMessageContaining("b").hasMessageContaining("c");
        }
    }
}
