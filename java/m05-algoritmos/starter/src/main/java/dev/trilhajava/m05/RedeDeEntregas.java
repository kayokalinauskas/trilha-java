package dev.trilhajava.m05;

import java.util.List;

/**
 * Exercício 05.05 (parte 1) — Rede de centros de distribuição. Trechos são de mão dupla.
 * Rode: mvn test -Dtest=GrafosTest
 */
public class RedeDeEntregas {

    /** Cada trecho é uma lista [origem, destino]. */
    public RedeDeEntregas(List<List<String>> trechos) {
        throw new UnsupportedOperationException("TODO: monte uma lista de adjacência");
    }

    /** Menor número de trechos (BFS); 0 se origem == destino; -1 se inalcançável ou desconhecido. */
    public int menorNumeroDeTrechos(String origem, String destino) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Centros do caminho mais curto, da origem ao destino; lista vazia se não houver. */
    public List<String> caminhoMaisCurto(String origem, String destino) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Número de componentes conexos. */
    public int regioesDesconectadas() {
        throw new UnsupportedOperationException("TODO");
    }
}
