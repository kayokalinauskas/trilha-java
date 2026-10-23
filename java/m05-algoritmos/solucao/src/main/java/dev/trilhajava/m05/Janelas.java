package dev.trilhajava.m05;

import java.util.HashMap;
import java.util.Map;

/**
 * Solução de referência do exercício 05.02.
 * Ideia comum: dois índices (inicio, fim) que só andam para a frente. Cada elemento entra e sai
 * da janela no máximo uma vez, então o total de passos é O(n), mesmo com um while dentro do for.
 */
public final class Janelas {

    private Janelas() {
    }

    /** Janela de tamanho FIXO: soma o que entra, subtrai o que sai. */
    public static double[] mediasMoveis(double[] valores, int k) {
        if (k < 1) {
            throw new IllegalArgumentException("k deve ser pelo menos 1");
        }
        if (valores.length < k) {
            return new double[0];
        }
        double[] medias = new double[valores.length - k + 1];
        double soma = 0;
        for (int i = 0; i < valores.length; i++) {
            soma += valores[i];
            if (i >= k) {
                soma -= valores[i - k];
            }
            if (i >= k - 1) {
                medias[i - k + 1] = soma / k;
            }
        }
        return medias;
    }

    /** Janela VARIÁVEL: ao achar repetição, pula o início para depois da ocorrência anterior. */
    public static int maiorSequenciaSemRepetir(String[] produtos) {
        Map<String, Integer> ultimaPosicao = new HashMap<>();
        int inicio = 0;
        int maior = 0;
        for (int fim = 0; fim < produtos.length; fim++) {
            Integer anterior = ultimaPosicao.put(produtos[fim], fim);
            if (anterior != null && anterior >= inicio) {
                inicio = anterior + 1;
            }
            maior = Math.max(maior, fim - inicio + 1);
        }
        return maior;
    }

    /** Expande até atingir a meta; depois encolhe pelo início enquanto ainda atinge. */
    public static int menorJanelaQueAtinge(int[] vendasPorHora, int meta) {
        int inicio = 0;
        long soma = 0;
        int menor = Integer.MAX_VALUE;
        for (int fim = 0; fim < vendasPorHora.length; fim++) {
            soma += vendasPorHora[fim];
            while (soma >= meta) {
                menor = Math.min(menor, fim - inicio + 1);
                soma -= vendasPorHora[inicio++];
            }
        }
        return menor == Integer.MAX_VALUE ? 0 : menor;
    }

    /** Expande; se estourar o orçamento, encolhe pelo início até voltar a caber. */
    public static int maiorPeriodoNoOrcamento(int[] gastosDiarios, int orcamento) {
        int inicio = 0;
        long soma = 0;
        int maior = 0;
        for (int fim = 0; fim < gastosDiarios.length; fim++) {
            soma += gastosDiarios[fim];
            while (soma > orcamento) {
                soma -= gastosDiarios[inicio++];
            }
            maior = Math.max(maior, fim - inicio + 1);
        }
        return maior;
    }
}
