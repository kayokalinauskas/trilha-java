package dev.trilhajava.m10.aulas.desempenho;

import java.util.Arrays;

/**
 * Resume uma lista de latências (em milissegundos) do jeito que um relatório de carga faz:
 * média, percentis e máximo. Os percentis usam o método do "posto mais próximo"
 * (nearest rank): o p95 é o valor que fica na posição ceil(0,95 × n) da lista ordenada.
 */
public record ResumoDeLatencias(int amostras, double mediaMs, long p50Ms, long p95Ms, long p99Ms, long maximoMs) {

    public static ResumoDeLatencias de(long[] latenciasMs) {
        if (latenciasMs.length == 0) {
            throw new IllegalArgumentException("Sem amostras não há percentil");
        }
        long[] ordenadas = latenciasMs.clone();
        Arrays.sort(ordenadas);
        double media = Arrays.stream(ordenadas).average().orElseThrow();
        return new ResumoDeLatencias(
                ordenadas.length,
                media,
                percentil(ordenadas, 50),
                percentil(ordenadas, 95),
                percentil(ordenadas, 99),
                ordenadas[ordenadas.length - 1]);
    }

    /** Recebe a lista já ordenada. Ex.: com 200 amostras, o p95 é a 190ª. */
    static long percentil(long[] ordenadas, double p) {
        int posto = (int) Math.ceil(p / 100.0 * ordenadas.length);
        return ordenadas[Math.max(posto, 1) - 1];
    }

    @Override
    public String toString() {
        return "amostras=%d média=%.1f ms p50=%d ms p95=%d ms p99=%d ms máx=%d ms"
                .formatted(amostras, mediaMs, p50Ms, p95Ms, p99Ms, maximoMs);
    }
}
