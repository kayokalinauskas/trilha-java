package dev.trilhajava.m10.aulas.tdd;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Uma parcela da simulação: número (1, 2, 3...), valor em reais e data de vencimento. */
public record Parcela(int numero, BigDecimal valor, LocalDate vencimento) {
}
