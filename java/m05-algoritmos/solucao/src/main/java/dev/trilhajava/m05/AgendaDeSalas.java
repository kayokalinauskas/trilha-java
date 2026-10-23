package dev.trilhajava.m05;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Solução de referência do exercício 05.06.
 * Quase todo problema de intervalos começa igual: ORDENAR pelo início. Depois, uma passada linear.
 */
public final class AgendaDeSalas {

    private static final Comparator<Intervalo> POR_INICIO = Comparator.comparingInt(Intervalo::inicio);

    private AgendaDeSalas() {
    }

    public static List<Intervalo> mesclar(List<Intervalo> intervalos) {
        List<Intervalo> ordenados = intervalos.stream().sorted(POR_INICIO).toList(); // cópia: não altera a entrada
        List<Intervalo> resultado = new ArrayList<>();
        for (Intervalo atual : ordenados) {
            if (!resultado.isEmpty() && atual.inicio() <= resultado.getLast().fim()) {
                Intervalo ultimo = resultado.removeLast();
                resultado.add(new Intervalo(ultimo.inicio(), Math.max(ultimo.fim(), atual.fim())));
            } else {
                resultado.add(atual);
            }
        }
        return List.copyOf(resultado);
    }

    public static boolean semConflitos(List<Intervalo> reunioes) {
        List<Intervalo> ordenadas = reunioes.stream().sorted(POR_INICIO).toList();
        for (int k = 1; k < ordenadas.size(); k++) {
            if (ordenadas.get(k).inicio() < ordenadas.get(k - 1).fim()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Percorre as reuniões por início; o heap guarda o término das salas ocupadas.
     * Se a sala que libera mais cedo já está livre, reaproveita; senão, abre outra.
     * O tamanho máximo do heap é a resposta.
     */
    public static int minimoDeSalas(List<Intervalo> reunioes) {
        PriorityQueue<Integer> terminos = new PriorityQueue<>();
        reunioes.stream().sorted(POR_INICIO).forEach(r -> {
            if (!terminos.isEmpty() && terminos.peek() <= r.inicio()) {
                terminos.poll();
            }
            terminos.add(r.fim());
        });
        return terminos.size();
    }

    public static List<Intervalo> horariosLivres(List<Intervalo> ocupados, int inicioDia, int fimDia) {
        List<Intervalo> livres = new ArrayList<>();
        int cursor = inicioDia;
        for (Intervalo o : mesclar(ocupados)) {
            if (o.inicio() > cursor) {
                livres.add(new Intervalo(cursor, Math.min(o.inicio(), fimDia)));
            }
            cursor = Math.max(cursor, o.fim());
            if (cursor >= fimDia) {
                return List.copyOf(livres);
            }
        }
        if (cursor < fimDia) {
            livres.add(new Intervalo(cursor, fimDia));
        }
        return List.copyOf(livres);
    }
}
