package dev.trilhajava.m05;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Solução de referência do exercício 05.05 (parte 1). */
public class RedeDeEntregas {

    private final Map<String, List<String>> vizinhos = new HashMap<>();

    public RedeDeEntregas(List<List<String>> trechos) {
        for (List<String> t : trechos) {
            String a = t.get(0);
            String b = t.get(1);
            vizinhos.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
            vizinhos.computeIfAbsent(b, k -> new ArrayList<>()).add(a); // mão dupla
        }
    }

    public int menorNumeroDeTrechos(String origem, String destino) {
        List<String> caminho = caminhoMaisCurto(origem, destino);
        return caminho.isEmpty() ? -1 : caminho.size() - 1;
    }

    /**
     * BFS visita os nós em "camadas" de distância crescente, então a primeira vez que alcança o destino
     * é pelo caminho com menos trechos. O mapa "veioDe" permite reconstruir o caminho de trás para a frente.
     */
    public List<String> caminhoMaisCurto(String origem, String destino) {
        if (!vizinhos.containsKey(origem) || !vizinhos.containsKey(destino)) {
            return List.of();
        }
        Map<String, String> veioDe = new HashMap<>();
        veioDe.put(origem, null);
        Deque<String> fila = new ArrayDeque<>(List.of(origem));
        while (!fila.isEmpty()) {
            String atual = fila.poll();
            if (atual.equals(destino)) {
                return reconstruir(veioDe, destino);
            }
            for (String proximo : vizinhos.get(atual)) {
                if (!veioDe.containsKey(proximo)) {   // marca ao ENFILEIRAR, não ao visitar
                    veioDe.put(proximo, atual);
                    fila.offer(proximo);
                }
            }
        }
        return List.of();
    }

    private static List<String> reconstruir(Map<String, String> veioDe, String destino) {
        List<String> caminho = new ArrayList<>();
        for (String no = destino; no != null; no = veioDe.get(no)) {
            caminho.add(no);
        }
        Collections.reverse(caminho);
        return List.copyOf(caminho);
    }

    /** DFS iterativa a partir de cada nó ainda não visitado: cada início é uma nova região. */
    public int regioesDesconectadas() {
        Set<String> visitados = new HashSet<>();
        int regioes = 0;
        for (String inicio : vizinhos.keySet()) {
            if (!visitados.add(inicio)) {
                continue;
            }
            regioes++;
            Deque<String> pilha = new ArrayDeque<>(List.of(inicio));
            while (!pilha.isEmpty()) {
                for (String v : vizinhos.get(pilha.pop())) {
                    if (visitados.add(v)) {
                        pilha.push(v);
                    }
                }
            }
        }
        return regioes;
    }
}
