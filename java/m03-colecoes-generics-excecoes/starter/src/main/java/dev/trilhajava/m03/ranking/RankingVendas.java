package dev.trilhajava.m03.ranking;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Exercício 03.02 — Ranking de produtos mais vendidos.
 * Rode: mvn test -Dtest=RankingVendasTest
 *
 * Restrição didática: não use Streams (eles chegam no módulo 04). Use Map.merge, Comparator e laços.
 */
public class RankingVendas {

    /** Faturamento total por produto, com as chaves em ordem alfabética. */
    public Map<String, BigDecimal> faturamentoPorProduto(List<Venda> vendas) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Os k produtos com mais unidades vendidas.
     * Desempate: maior faturamento primeiro; depois ordem alfabética do nome.
     */
    public List<ItemRanking> topPorQuantidade(List<Venda> vendas, int k) {
        throw new UnsupportedOperationException("TODO");
    }
}
