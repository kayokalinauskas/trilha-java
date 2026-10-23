package dev.trilhajava.m06;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Solução de referência do exercício 06.01.
 *
 * - ConcurrentHashMap.computeIfAbsent é atômico: duas threads nunca criam dois contadores para a mesma chave.
 * - LongAdder espalha as somas em células por thread e só as junta na leitura: sob muita disputa,
 *   escala bem melhor que AtomicLong (que faz todas as threads brigarem pela mesma variável).
 */
public class ContadorDeAcessos {

    private final Map<String, LongAdder> porEndpoint = new ConcurrentHashMap<>();

    public void registrar(String endpoint) {
        porEndpoint.computeIfAbsent(endpoint, k -> new LongAdder()).increment();
    }

    public long total(String endpoint) {
        LongAdder contador = porEndpoint.get(endpoint);
        return contador == null ? 0 : contador.sum();
    }

    public long totalGeral() {
        return porEndpoint.values().stream().mapToLong(LongAdder::sum).sum();
    }

    public List<String> topEndpoints(int n) {
        // Leitura "fotográfica": sob escrita concorrente, os números podem estar alguns milissegundos atrasados.
        return porEndpoint.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().sum()))
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }
}
