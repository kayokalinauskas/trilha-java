package dev.trilhajava.m05;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

/**
 * Solução de referência do exercício 05.05 (parte 2) — algoritmo de Kahn.
 * Grau de entrada = quantas dependências ainda faltam. Quem tem grau zero está pronto para o build.
 */
public final class OrdemDeBuild {

    private OrdemDeBuild() {
    }

    public static List<String> ordenar(Map<String, List<String>> dependencias) {
        Map<String, Integer> faltam = new HashMap<>();
        Map<String, List<String>> liberaQuem = new HashMap<>(); // dependência → módulos que dependem dela
        dependencias.forEach((modulo, deps) -> {
            faltam.merge(modulo, deps.size(), Integer::sum);
            for (String d : deps) {
                faltam.putIfAbsent(d, 0);
                liberaQuem.computeIfAbsent(d, k -> new ArrayList<>()).add(modulo);
            }
        });

        PriorityQueue<String> prontos = new PriorityQueue<>(); // ordem alfabética entre os prontos
        faltam.forEach((modulo, n) -> {
            if (n == 0) {
                prontos.add(modulo);
            }
        });

        List<String> ordem = new ArrayList<>();
        while (!prontos.isEmpty()) {
            String modulo = prontos.poll();
            ordem.add(modulo);
            for (String dependente : liberaQuem.getOrDefault(modulo, List.of())) {
                if (faltam.merge(dependente, -1, Integer::sum) == 0) {
                    prontos.add(dependente);
                }
            }
        }

        if (ordem.size() < faltam.size()) {
            // Quem nunca chegou a zero está em um ciclo (ou depende de um).
            TreeSet<String> presos = new TreeSet<>(faltam.keySet());
            presos.removeAll(ordem);
            throw new CicloDeDependenciaException(presos);
        }
        return List.copyOf(ordem);
    }
}
