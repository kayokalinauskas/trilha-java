package dev.trilhajava.m03.metricas;

import java.util.Collection;
import java.util.Map;

/**
 * Exercício 03.04 — as ASSINATURAS já estão prontas (os testes dependem delas).
 * Seu trabalho: implementar os corpos e conseguir explicar, com suas palavras, cada wildcard.
 * Rode: mvn test -Dtest=MetricasGenericasTest
 */
public final class Estatisticas {

    private Estatisticas() {
    }

    /** Por que "? extends Number" e não "Number"? Tente trocar e veja o que o compilador diz no teste. */
    public static double media(Collection<? extends Number> valores) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Por que "Comparable<? super T>" e não "Comparable<T>"? Pense em LocalDate. */
    public static <T extends Comparable<? super T>> T maximo(Collection<? extends T> valores) {
        throw new UnsupportedOperationException("TODO: coleção vazia lança NoSuchElementException");
    }

    /** Producer extends, Consumer super: copia para o destino os valores estritamente maiores que o limite. */
    public static void copiarAcimaDe(Collection<? extends Number> origem, Collection<? super Number> destino, double limite) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Média das leituras agrupadas por sensor, com as chaves em ordem alfabética. */
    public static Map<String, Double> mediaPorSensor(Collection<? extends Leitura<? extends Number>> leituras) {
        throw new UnsupportedOperationException("TODO");
    }
}
