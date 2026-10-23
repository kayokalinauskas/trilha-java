package dev.trilhajava.m10.aulas.fatias;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** O corpo do POST /produtos/{sku}/baixas. */
public record BaixaDeEstoque(
        @NotNull(message = "é obrigatória") @Positive(message = "deve ser maior que zero") Integer quantidade) {
}
