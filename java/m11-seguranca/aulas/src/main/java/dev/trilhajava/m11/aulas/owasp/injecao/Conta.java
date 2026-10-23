package dev.trilhajava.m11.aulas.owasp.injecao;

import java.math.BigDecimal;

/** Uma conta da fintech, como sai do banco. Saldo em BigDecimal, nunca double. */
public record Conta(long id, String titular, String agencia, BigDecimal saldo) {
}
