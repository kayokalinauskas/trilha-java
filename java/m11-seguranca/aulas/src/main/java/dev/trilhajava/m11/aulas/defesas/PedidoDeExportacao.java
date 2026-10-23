package dev.trilhajava.m11.aulas.defesas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** O corpo do POST /api/exportacoes. É um record: dois pedidos iguais têm equals verdadeiro. */
public record PedidoDeExportacao(@NotNull LocalDate de, @NotNull LocalDate ate, @NotBlank String formato) {
}
