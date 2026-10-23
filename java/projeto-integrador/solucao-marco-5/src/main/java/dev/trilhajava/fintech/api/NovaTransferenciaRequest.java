package dev.trilhajava.fintech.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record NovaTransferenciaRequest(
        @NotNull UUID origem,
        @NotNull UUID destino,
        @NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal valor) {
}
