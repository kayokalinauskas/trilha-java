package dev.trilhajava.m08.reservas;

import java.time.LocalDate;

public record Reserva(long id, long quartoId, String hospede, LocalDate checkIn, LocalDate checkOut) {

    /** Períodos [checkIn, checkOut) se sobrepõem? O dia do check-out fica livre para o próximo hóspede. */
    boolean conflitaCom(long outroQuarto, LocalDate entrada, LocalDate saida) {
        return quartoId == outroQuarto && entrada.isBefore(checkOut) && checkIn.isBefore(saida);
    }
}
