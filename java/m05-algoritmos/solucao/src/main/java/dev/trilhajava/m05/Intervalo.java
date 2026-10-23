package dev.trilhajava.m05;

/** Intervalo semiaberto [inicio, fim) em minutos desde a meia-noite. */
public record Intervalo(int inicio, int fim) {

    public Intervalo {
        if (inicio >= fim) {
            throw new IllegalArgumentException("Início deve ser antes do fim: " + inicio + " >= " + fim);
        }
    }
}
