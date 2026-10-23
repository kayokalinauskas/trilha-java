package dev.trilhajava.m08.reservas;

/** Regra de negócio violada com dados sintaticamente válidos (candidata a 422). */
public class PeriodoInvalidoException extends RuntimeException {

    public PeriodoInvalidoException(String motivo) {
        super(motivo);
    }
}
