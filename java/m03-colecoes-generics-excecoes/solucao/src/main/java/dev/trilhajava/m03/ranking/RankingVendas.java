package dev.trilhajava.m03.ranking;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/** Solução de referência do exercício 03.02. */
public class RankingVendas {

    /** Ordem do ranking: quantidade desc, faturamento desc, nome asc. */
    private static final Comparator<ItemRanking> ORDEM_DO_RANKING = Comparator
            .comparingInt(ItemRanking::quantidade).reversed()
            .thenComparing(ItemRanking::faturamento, Comparator.reverseOrder())
            .thenComparing(ItemRanking::produto);

    public Map<String, BigDecimal> faturamentoPorProduto(List<Venda> vendas) {
        SortedMap<String, BigDecimal> faturamento = new TreeMap<>(); // TreeMap: chaves sempre ordenadas
        for (Venda venda : vendas) {
            faturamento.merge(venda.produto(), venda.total(), BigDecimal::add);
        }
        return faturamento;
    }

    public List<ItemRanking> topPorQuantidade(List<Venda> vendas, int k) {
        if (k < 1) {
            throw new IllegalArgumentException("k deve ser pelo menos 1");
        }
        // Duas agregações em uma passada. HashMap aqui: a ordem final vem do sort, não do mapa.
        Map<String, Integer> quantidades = new HashMap<>();
        Map<String, BigDecimal> faturamentos = new HashMap<>();
        for (Venda venda : vendas) {
            quantidades.merge(venda.produto(), venda.quantidade(), Integer::sum);
            faturamentos.merge(venda.produto(), venda.total(), BigDecimal::add);
        }

        List<ItemRanking> itens = new ArrayList<>(quantidades.size());
        for (Map.Entry<String, Integer> e : quantidades.entrySet()) {
            itens.add(new ItemRanking(e.getKey(), e.getValue(), faturamentos.get(e.getKey())));
        }
        itens.sort(ORDEM_DO_RANKING);
        return List.copyOf(itens.subList(0, Math.min(k, itens.size())));
    }
}
