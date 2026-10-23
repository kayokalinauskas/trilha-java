package dev.trilhajava.m05;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/** Solução de referência do exercício 05.03. */
public final class Pilhas {

    private static final Map<Character, Character> ABRE_PARA = Map.of(')', '(', ']', '[', '}', '{');

    private Pilhas() {
    }

    public static boolean balanceada(String expressao) {
        Deque<Character> pilha = new ArrayDeque<>();
        for (char c : expressao.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                pilha.push(c);
            } else if (ABRE_PARA.containsKey(c)) {
                if (pilha.isEmpty() || !pilha.pop().equals(ABRE_PARA.get(c))) {
                    return false;
                }
            }
        }
        return pilha.isEmpty(); // sobrou algo aberto?
    }

    /**
     * Pilha monotônica decrescente de ÍNDICES: cada dia fica na pilha até aparecer um mais quente,
     * que "resolve" todos os mais frios do topo. Cada índice entra e sai uma vez: O(n).
     */
    public static int[] diasAteTemperaturaMaior(int[] temperaturas) {
        int[] resposta = new int[temperaturas.length];
        Deque<Integer> pendentes = new ArrayDeque<>();
        for (int hoje = 0; hoje < temperaturas.length; hoje++) {
            while (!pendentes.isEmpty() && temperaturas[hoje] > temperaturas[pendentes.peek()]) {
                int dia = pendentes.pop();
                resposta[dia] = hoje - dia;
            }
            pendentes.push(hoje);
        }
        return resposta;
    }

    public static long avaliarRpn(String expressao) {
        Deque<Long> pilha = new ArrayDeque<>();
        for (String token : expressao.trim().split("\\s+")) {
            switch (token) {
                case "+", "-", "*", "/" -> {
                    if (pilha.size() < 2) {
                        throw new IllegalArgumentException("Operador '" + token + "' sem operandos suficientes");
                    }
                    long b = pilha.pop();
                    long a = pilha.pop();
                    pilha.push(switch (token) {
                        case "+" -> a + b;
                        case "-" -> a - b;
                        case "*" -> a * b;
                        default -> a / b;
                    });
                }
                default -> {
                    try {
                        pilha.push(Long.parseLong(token));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Token inválido: " + token, e);
                    }
                }
            }
        }
        if (pilha.size() != 1) {
            throw new IllegalArgumentException("Expressão malformada: sobraram " + pilha.size() + " valores");
        }
        return pilha.pop();
    }

    public static String simplificarCaminho(String caminho) {
        Deque<String> partes = new ArrayDeque<>();
        for (String parte : caminho.split("/")) {
            switch (parte) {
                case "", "." -> { } // ignora barras duplicadas e "diretório atual"
                case ".." -> partes.pollLast(); // sobe um nível (na raiz, não faz nada)
                default -> partes.addLast(parte);
            }
        }
        return "/" + String.join("/", partes);
    }
}
