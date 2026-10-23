package dev.trilhajava.m03.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 03.01 — Consolidação do catálogo de peças")
class ConsolidadorCatalogoTest {

    private static final LocalDate JAN = LocalDate.of(2026, 1, 10);
    private static final LocalDate MAR = LocalDate.of(2026, 3, 5);

    private static RegistroPeca reg(String codigo, String nome, String preco, LocalDate data) {
        return new RegistroPeca(codigo, nome, preco == null ? null : new BigDecimal(preco), data);
    }

    private final ConsolidadorCatalogo consolidador = new ConsolidadorCatalogo();

    @Nested
    @DisplayName("Peca: igualdade pelo código normalizado")
    class Igualdade {
        @Test
        @DisplayName("mesmo código com caixa e espaços diferentes é a mesma peça")
        void mesmoCodigo() {
            Peca a = new Peca(" ab-123 ", "Filtro", new BigDecimal("35.00"), JAN);
            Peca b = new Peca("AB-123", "Filtro Premium", new BigDecimal("38.50"), MAR);
            assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
            assertThat(a.codigo()).isEqualTo("AB-123");
        }

        @Test
        @DisplayName("em um HashSet, a segunda inserção do mesmo código retorna false")
        void hashSet() {
            Set<Peca> set = new HashSet<>();
            assertThat(set.add(new Peca("xy-1", "Vela", BigDecimal.TEN, JAN))).isTrue();
            assertThat(set.add(new Peca("XY-1", "Vela NGK", BigDecimal.ONE, MAR))).isFalse();
            assertThat(set).hasSize(1);
        }

        @Test
        @DisplayName("código em branco é recusado na criação")
        void codigoInvalido() {
            assertThatThrownBy(() -> new Peca("  ", "X", BigDecimal.ONE, JAN)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("consolidação")
    class Consolidacao {
        @Test
        @DisplayName("exemplo do enunciado: 3 peças, com a versão mais recente do AB-123")
        void exemplo() {
            List<RegistroPeca> legado = List.of(reg("ab-123", "Filtro de óleo", "35.00", JAN), reg("cd-999", "Vela", "12.00", JAN));
            List<RegistroPeca> novo = List.of(reg("AB-123", "Filtro de Óleo Premium", "38.50", MAR), reg("ef-001", "Correia", "60.00", MAR));

            ResultadoConsolidacao r = consolidador.consolidar(legado, novo);

            assertThat(r.pecas()).extracting(Peca::codigo).containsExactly("AB-123", "CD-999", "EF-001");
            assertThat(r.pecas().get(0).preco()).isEqualByComparingTo("38.50");
            assertThat(r.rejeitados()).isZero();
        }

        @Test
        @DisplayName("em empate de data, a versão do sistema novo vence")
        void empateNovoVence() {
            ResultadoConsolidacao r = consolidador.consolidar(
                    List.of(reg("a1", "Legado", "10.00", MAR)),
                    List.of(reg("A1", "Novo", "11.00", MAR)));
            assertThat(r.pecas()).singleElement().extracting(Peca::nome).isEqualTo("Novo");
        }

        @Test
        @DisplayName("versão mais recente vence mesmo vindo do legado")
        void legadoMaisRecente() {
            ResultadoConsolidacao r = consolidador.consolidar(
                    List.of(reg("a1", "Legado recente", "10.00", MAR)),
                    List.of(reg("A1", "Novo antigo", "11.00", JAN)));
            assertThat(r.pecas()).singleElement().extracting(Peca::nome).isEqualTo("Legado recente");
        }

        @Test
        @DisplayName("registros inválidos são contados e ignorados, sem quebrar o resto")
        void invalidos() {
            List<RegistroPeca> legado = List.of(reg(null, "Sem código", "1.00", JAN), reg("ok-1", "Válida", "5.00", JAN));
            List<RegistroPeca> novo = List.of(reg("  ", "Código vazio", "1.00", JAN), reg("ok-2", "Sem preço", null, JAN));
            ResultadoConsolidacao r = consolidador.consolidar(legado, novo);
            assertThat(r.pecas()).extracting(Peca::codigo).containsExactly("OK-1");
            assertThat(r.rejeitados()).isEqualTo(3);
        }

        @Test
        @DisplayName("fontes vazias geram catálogo vazio")
        void vazias() {
            ResultadoConsolidacao r = consolidador.consolidar(List.of(), List.of());
            assertThat(r.pecas()).isEmpty();
            assertThat(r.rejeitados()).isZero();
        }

        @Test
        @DisplayName("duplicados dentro da MESMA fonte também são consolidados")
        void duplicadosNaMesmaFonte() {
            ResultadoConsolidacao r = consolidador.consolidar(
                    List.of(reg("z-9", "Antiga", "1.00", JAN), reg("Z-9", "Atual", "2.00", MAR)), List.of());
            assertThat(r.pecas()).singleElement().extracting(Peca::nome).isEqualTo("Atual");
        }

        @Test
        @DisplayName("o resultado não pode ser modificado")
        void imutavel() {
            ResultadoConsolidacao r = consolidador.consolidar(List.of(reg("a", "A", "1", JAN)), List.of());
            assertThatThrownBy(() -> r.pecas().clear()).isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
