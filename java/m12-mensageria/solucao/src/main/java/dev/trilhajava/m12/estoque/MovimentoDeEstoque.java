package dev.trilhajava.m12.estoque;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Entrada (quantidade positiva) ou saída (negativa) de um SKU. É o evento que trafega no tópico. */
public record MovimentoDeEstoque(UUID id, String sku, int quantidade, Instant ocorridoEm) {

    public MovimentoDeEstoque {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ocorridoEm, "ocorridoEm");
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku obrigatório");
        }
        if (quantidade == 0) {
            throw new IllegalArgumentException("quantidade não pode ser zero");
        }
    }

    public static MovimentoDeEstoque de(String sku, int quantidade) {
        return new MovimentoDeEstoque(UUID.randomUUID(), sku, quantidade, Instant.now());
    }
}
