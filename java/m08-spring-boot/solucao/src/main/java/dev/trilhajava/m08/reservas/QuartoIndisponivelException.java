package dev.trilhajava.m08.reservas;

import java.time.LocalDate;

public class QuartoIndisponivelException extends RuntimeException {

    private final long quartoId;

    public QuartoIndisponivelException(long quartoId, LocalDate checkIn, LocalDate checkOut) {
        super("O quarto " + quartoId + " já está reservado em parte do período de " + checkIn + " a " + checkOut);
        this.quartoId = quartoId;
    }

    public long quartoId() {
        return quartoId;
    }
}
