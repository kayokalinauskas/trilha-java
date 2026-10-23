package dev.trilhajava.m05;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Solução de referência do exercício 05.01. Padrão comum: trocar uma busca O(n) por um HashMap/HashSet O(1). */
public final class AuditoriaTransacoes {

    private AuditoriaTransacoes() {
    }

    /** Two Sum: para cada valor, o "complemento" que falta já foi visto? Tempo O(n), memória O(n). */
    public static int[] parQueSoma(long[] valoresEmCentavos, long alvo) {
        Map<Long, Integer> vistos = new HashMap<>();
        for (int j = 0; j < valoresEmCentavos.length; j++) {
            Integer i = vistos.get(alvo - valoresEmCentavos[j]);
            if (i != null) {
                return new int[] {i, j};
            }
            vistos.putIfAbsent(valoresEmCentavos[j], j); // mantém o primeiro índice de cada valor
        }
        return new int[0];
    }

    /** Guarda a última posição de cada id; basta comparar a distância. */
    public static boolean duplicataEmJanela(String[] ids, int k) {
        Map<String, Integer> ultimaPosicao = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            Integer anterior = ultimaPosicao.put(ids[i], i);
            if (anterior != null && i - anterior <= k) {
                return true;
            }
        }
        return false;
    }

    /** LinkedHashMap conta e preserva a ordem de chegada: duas passadas, O(n). */
    public static Optional<String> primeiroClienteUnico(String[] clientes) {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (String c : clientes) {
            contagem.merge(c, 1, Integer::sum);
        }
        return contagem.entrySet().stream().filter(e -> e.getValue() == 1).map(Map.Entry::getKey).findFirst();
    }

    /**
     * Só começa a contar uma sequência a partir do seu INÍCIO (quando n-1 não existe).
     * Assim cada número é visitado no máximo duas vezes: O(n), sem ordenar (que seria O(n log n)).
     */
    public static int maiorSequenciaDeDias(int[] dias) {
        Set<Integer> presentes = new HashSet<>();
        for (int d : dias) {
            presentes.add(d);
        }
        int maior = 0;
        for (int d : presentes) {
            if (presentes.contains(d - 1)) {
                continue; // não é início de sequência
            }
            int tamanho = 1;
            while (presentes.contains(d + tamanho)) {
                tamanho++;
            }
            maior = Math.max(maior, tamanho);
        }
        return maior;
    }
}
