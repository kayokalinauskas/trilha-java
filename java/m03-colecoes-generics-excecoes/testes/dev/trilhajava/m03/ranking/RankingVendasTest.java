package dev.trilhajava.m03.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 03.02 — Ranking de produtos mais vendidos")
class RankingVendasTest {

    private static Venda v(String produto, int qtd, String preco) {
        return new Venda(produto, qtd, new BigDecimal(preco));
    }

    private final List<Venda> vendas = List.of(
            v("Café 500g", 3, "25.90"),
            v("Filtro", 10, "4.50"),
            v("Café 500g", 4, "24.90"),
            v("Caneca", 2, "39.90"),
            v("Filtro", 1, "4.50"),
            v("Moedor", 1, "189.00"),
            v("Caneca", 5, "39.90"));

    private final RankingVendas ranking = new RankingVendas();

    @Test
    @DisplayName("soma quantidade e faturamento por produto")
    void agrega() {
        Map<String, BigDecimal> faturamento = ranking.faturamentoPorProduto(vendas);
        assertThat(faturamento).containsOnlyKeys("Café 500g", "Filtro", "Caneca", "Moedor");
        assertThat(faturamento.get("Café 500g")).isEqualByComparingTo("177.30"); // 3*25.90 + 4*24.90
        assertThat(faturamento.get("Caneca")).isEqualByComparingTo("279.30");
    }

    @Test
    @DisplayName("faturamento vem em ordem alfabética de produto")
    void ordemAlfabetica() {
        assertThat(ranking.faturamentoPorProduto(vendas).keySet()).containsExactly("Café 500g", "Caneca", "Filtro", "Moedor");
    }

    @Test
    @DisplayName("top 3 por quantidade (Caneca e Café empatam em 7 unidades; Caneca faturou mais)")
    void top3() {
        List<ItemRanking> top = ranking.topPorQuantidade(vendas, 3);
        assertThat(top).extracting(ItemRanking::produto).containsExactly("Filtro", "Caneca", "Café 500g");
        assertThat(top.get(0).quantidade()).isEqualTo(11);
        assertThat(top.get(0).faturamento()).isEqualByComparingTo("49.50");
    }

    @Test
    @DisplayName("empate em quantidade: maior faturamento primeiro; persistindo, ordem alfabética")
    void desempate() {
        List<Venda> empatadas = List.of(v("B", 2, "10.00"), v("A", 2, "10.00"), v("C", 2, "50.00"));
        assertThat(ranking.topPorQuantidade(empatadas, 3)).extracting(ItemRanking::produto).containsExactly("C", "A", "B");
    }

    @Test
    @DisplayName("k maior que o número de produtos devolve todos")
    void kGrande() {
        assertThat(ranking.topPorQuantidade(vendas, 50)).hasSize(4);
    }

    @Test
    @DisplayName("sem vendas, ranking vazio")
    void semVendas() {
        assertThat(ranking.topPorQuantidade(List.of(), 3)).isEmpty();
        assertThat(ranking.faturamentoPorProduto(List.of())).isEmpty();
    }

    @Test
    @DisplayName("k precisa ser positivo")
    void kInvalido() {
        assertThatThrownBy(() -> ranking.topPorQuantidade(vendas, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("venda com quantidade não positiva é recusada na criação")
    void vendaInvalida() {
        assertThatThrownBy(() -> v("X", 0, "1.00")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> v(" ", 1, "1.00")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("o ranking devolvido não pode ser alterado")
    void imutavel() {
        List<ItemRanking> top = ranking.topPorQuantidade(vendas, 2);
        assertThatThrownBy(top::clear).isInstanceOf(UnsupportedOperationException.class);
    }
}
