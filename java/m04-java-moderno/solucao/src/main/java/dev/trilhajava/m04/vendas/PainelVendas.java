package dev.trilhajava.m04.vendas;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collector;

/** Solução de referência do exercício 04.01. */
public class PainelVendas {

    /** Collector reutilizável: soma os valores dos pedidos em BigDecimal. */
    private static final Collector<Pedido, ?, BigDecimal> SOMA_VALOR =
            reducing(BigDecimal.ZERO, Pedido::valor, BigDecimal::add);

    public Map<String, BigDecimal> faturamentoPorCategoria(List<Pedido> pedidos) {
        return pedidos.stream()
                .filter(Pedido::pago)
                .collect(groupingBy(Pedido::categoria, TreeMap::new, SOMA_VALOR));
    }

    public BigDecimal ticketMedio(List<Pedido> pedidos) {
        List<BigDecimal> valores = pedidos.stream().filter(Pedido::pago).map(Pedido::valor).toList();
        if (valores.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal soma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_EVEN);
    }

    public List<String> topClientes(List<Pedido> pedidos, int n) {
        return pedidos.stream()
                .filter(Pedido::pago)
                .collect(groupingBy(Pedido::cliente, SOMA_VALOR))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList(); // Stream.toList() (Java 16+) já devolve lista imutável
    }

    public Map<StatusPedido, Long> contagemPorStatus(List<Pedido> pedidos) {
        Map<StatusPedido, Long> contagem = pedidos.stream()
                .collect(groupingBy(Pedido::status, () -> new EnumMap<>(StatusPedido.class), counting()));
        // groupingBy só cria chaves que aparecem nos dados: completa os status ausentes com zero.
        for (StatusPedido status : StatusPedido.values()) {
            contagem.putIfAbsent(status, 0L);
        }
        return contagem;
    }

    public Map<YearMonth, BigDecimal> faturamentoMensal(List<Pedido> pedidos) {
        return pedidos.stream()
                .filter(Pedido::pago)
                .collect(groupingBy(p -> YearMonth.from(p.data()), TreeMap::new, SOMA_VALOR));
    }

    public Map<Boolean, List<String>> particionarPorValor(List<Pedido> pedidos, BigDecimal limite) {
        return pedidos.stream()
                .filter(Pedido::pago)
                .collect(partitioningBy(p -> p.valor().compareTo(limite) >= 0, mapping(Pedido::id, toList())));
    }

    public Map<String, List<String>> clientesPorCategoria(List<Pedido> pedidos) {
        return pedidos.stream()
                .filter(Pedido::pago)
                .collect(groupingBy(Pedido::categoria, TreeMap::new,
                        mapping(Pedido::cliente, collectingAndThen(toCollection(TreeSet::new), List::copyOf))));
    }
}
