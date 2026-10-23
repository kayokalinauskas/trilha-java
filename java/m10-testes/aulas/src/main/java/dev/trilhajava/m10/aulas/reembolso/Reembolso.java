package dev.trilhajava.m10.aulas.reembolso;

import java.math.BigDecimal;
import java.time.Instant;

/** O registro de um reembolso concluído. */
public record Reembolso(String numeroPedido, BigDecimal valor, String codigoEstorno, Instant realizadoEm) {
}
