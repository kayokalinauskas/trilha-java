package dev.trilhajava.m05;

import java.util.function.IntPredicate;

/**
 * Solução de referência do exercício 05.04.
 *
 * O molde que resolve quase tudo: procure o PRIMEIRO valor em [baixo, alto) para o qual uma condição
 * monotônica (false...false true...true) vira true. Mantenha a invariante e encerre quando baixo == alto.
 */
public final class BuscaBinaria {

    private BuscaBinaria() {
    }

    public static int posicaoDeInsercao(int[] ordenado, int alvo) {
        int baixo = 0;
        int alto = ordenado.length;            // intervalo semiaberto [baixo, alto)
        while (baixo < alto) {
            int meio = baixo + (alto - baixo) / 2; // evita overflow de (baixo + alto) / 2
            if (ordenado[meio] >= alvo) {
                alto = meio;                   // meio pode ser a resposta
            } else {
                baixo = meio + 1;
            }
        }
        return baixo;
    }

    public static int primeiraVersaoComBug(int totalVersoes, IntPredicate temBug) {
        int baixo = 1;
        int alto = totalVersoes;
        while (baixo < alto) {
            int meio = baixo + (alto - baixo) / 2;
            if (temBug.test(meio)) {
                alto = meio;
            } else {
                baixo = meio + 1;
            }
        }
        return temBug.test(baixo) ? baixo : -1;
    }

    /** Em cada passo, pelo menos uma das metades está ordenada: decide se o alvo está nela. */
    public static int buscarEmRotacionado(int[] nums, int alvo) {
        int baixo = 0;
        int alto = nums.length - 1;
        while (baixo <= alto) {
            int meio = baixo + (alto - baixo) / 2;
            if (nums[meio] == alvo) {
                return meio;
            }
            if (nums[baixo] <= nums[meio]) {           // metade esquerda ordenada
                if (alvo >= nums[baixo] && alvo < nums[meio]) {
                    alto = meio - 1;
                } else {
                    baixo = meio + 1;
                }
            } else {                                   // metade direita ordenada
                if (alvo > nums[meio] && alvo <= nums[alto]) {
                    baixo = meio + 1;
                } else {
                    alto = meio - 1;
                }
            }
        }
        return -1;
    }

    /**
     * Busca binária NA RESPOSTA: capacidades vão de max(pesos) a soma(pesos), e "cabe em D dias"
     * é monotônico (se cabe com C, cabe com C+1). Procuramos a primeira capacidade que cabe.
     */
    public static int menorCapacidade(int[] pesos, int dias) {
        int baixo = 0;
        int alto = 0;
        for (int p : pesos) {
            baixo = Math.max(baixo, p);
            alto += p;
        }
        while (baixo < alto) {
            int meio = baixo + (alto - baixo) / 2;
            if (diasNecessarios(pesos, meio) <= dias) {
                alto = meio;
            } else {
                baixo = meio + 1;
            }
        }
        return baixo;
    }

    private static int diasNecessarios(int[] pesos, int capacidade) {
        int dias = 1;
        int carga = 0;
        for (int p : pesos) {
            if (carga + p > capacidade) {
                dias++;
                carga = 0;
            }
            carga += p;
        }
        return dias;
    }
}
