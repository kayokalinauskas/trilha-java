package dev.trilhajava.m05;

import java.util.function.IntPredicate;

/**
 * Exercício 05.04 — Busca binária. Todos em O(log n) (menorCapacidade: O(n log S)).
 * Rode: mvn test -Dtest=BuscaBinariaTest
 */
public final class BuscaBinaria {

    private BuscaBinaria() {
    }

    /** Menor índice i tal que ordenado[i] >= alvo (ou length, se todos forem menores). */
    public static int posicaoDeInsercao(int[] ordenado, int alvo) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Versões 1..total; a partir da primeira com bug, todas têm bug. Devolve a primeira, ou -1. */
    public static int primeiraVersaoComBug(int totalVersoes, IntPredicate temBug) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Array crescente, sem repetidos, rotacionado num ponto desconhecido. Índice do alvo ou -1. */
    public static int buscarEmRotacionado(int[] nums, int alvo) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Pacotes devem sair NA ORDEM; cada dia o caminhão leva pacotes até a capacidade. Menor capacidade possível. */
    public static int menorCapacidade(int[] pesos, int dias) {
        throw new UnsupportedOperationException("TODO");
    }
}
