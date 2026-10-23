package dev.trilhajava.m10.aulas.fatias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * O corpo do POST /produtos. As mensagens são fixas para não dependerem do idioma da JVM.
 * {@code quantidadeInicial} é {@code Integer}, e não {@code int}: se o campo faltar no JSON,
 * quem responde é a validação (400 com o campo listado), e não o Jackson ("Failed to read request").
 */
public record NovoProduto(
        @NotBlank(message = "é obrigatório") @Size(max = 20, message = "tem no máximo 20 caracteres") String sku,
        @NotBlank(message = "é obrigatório") String nome,
        @NotNull(message = "é obrigatório") @Positive(message = "deve ser maior que zero") BigDecimal preco,
        @NotNull(message = "é obrigatória") @PositiveOrZero(message = "não pode ser negativa") Integer quantidadeInicial,
        @NotBlank(message = "é obrigatória") String categoria) {
}
