package dev.trilhajava.m13.aulas.solid;

import java.math.BigDecimal;
import java.util.Objects;

/** Um desconto concedido por uma regra, com o motivo que aparece no resumo do pedido. */
public record Desconto(String motivo, BigDecimal valor) {

    public Desconto {
        Objects.requireNonNull(motivo, "motivo");
        Objects.requireNonNull(valor, "valor");
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("desconto negativo: " + valor);
        }
    }
}
