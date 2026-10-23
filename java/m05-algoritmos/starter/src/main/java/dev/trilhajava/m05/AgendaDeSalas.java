package dev.trilhajava.m05;

import java.util.List;

/**
 * Exercício 05.06 — Agenda de salas de reunião. Alvo: O(n log n).
 * Rode: mvn test -Dtest=AgendaDeSalasTest
 */
public final class AgendaDeSalas {

    private AgendaDeSalas() {
    }

    /** Une intervalos que se sobrepõem ou se encostam; resultado ordenado. Não altera a entrada. */
    public static List<Intervalo> mesclar(List<Intervalo> intervalos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** true se nenhum par de reuniões se sobrepõe (encostar é permitido). */
    public static boolean semConflitos(List<Intervalo> reunioes) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Menor número de salas para acomodar todas as reuniões. */
    public static int minimoDeSalas(List<Intervalo> reunioes) {
        throw new UnsupportedOperationException("TODO: dica — PriorityQueue de horários de término");
    }

    /** Lacunas livres entre inicioDia e fimDia, considerando os horários ocupados. */
    public static List<Intervalo> horariosLivres(List<Intervalo> ocupados, int inicioDia, int fimDia) {
        throw new UnsupportedOperationException("TODO");
    }
}
