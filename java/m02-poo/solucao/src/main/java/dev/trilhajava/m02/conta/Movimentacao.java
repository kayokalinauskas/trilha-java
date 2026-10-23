package dev.trilhajava.m02.conta;

import java.math.BigDecimal;
import java.time.Instant;

/** Uma linha do extrato. Já pronta — records são imutáveis por natureza (você vai estudá-los na aula 5). */
public record Movimentacao(TipoMovimentacao tipo, BigDecimal valor, BigDecimal saldoApos, Instant momento) {
}
