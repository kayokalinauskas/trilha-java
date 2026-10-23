package dev.trilhajava.m04.eventos;

import java.math.BigDecimal;

/** Estado reconstruído (a "projeção"). Já pronto. */
public record EstadoConta(String contaId, String titular, BigDecimal saldo, boolean bloqueada, int eventosAplicados) {
}
