package dev.trilhajava.m06;

import java.util.Optional;
import java.util.Set;

/**
 * Exercício 06.02 — Sala de cinema com reservas concorrentes.
 * Rode: mvn test -Dtest=SalaDeCinemaTest
 *
 * O bug clássico: "check-then-act" — verificar se o assento está livre e DEPOIS reservar,
 * em dois passos que outra thread pode intercalar.
 */
public class SalaDeCinema {

    public SalaDeCinema(int totalAssentos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Reserva se livre. Assento fora de 0..total-1: IllegalArgumentException. */
    public boolean reservar(int assento, String cliente) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Reserva todos ou nenhum, de forma atômica. */
    public boolean reservarVarios(Set<Integer> assentos, String cliente) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<String> ocupante(int assento) {
        throw new UnsupportedOperationException("TODO");
    }

    public int livres() {
        throw new UnsupportedOperationException("TODO");
    }
}
