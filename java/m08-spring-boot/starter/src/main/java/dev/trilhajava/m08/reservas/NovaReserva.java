package dev.trilhajava.m08.reservas;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record NovaReserva(
        @NotNull(message = "quarto é obrigatório") Long quartoId,
        @NotBlank(message = "hóspede é obrigatório") String hospede,
        @NotNull(message = "check-in é obrigatório") @FutureOrPresent(message = "check-in não pode estar no passado") LocalDate checkIn,
        @NotNull(message = "check-out é obrigatório") LocalDate checkOut) {
}
