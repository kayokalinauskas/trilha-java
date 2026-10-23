package dev.trilhajava.m07;

import java.math.BigDecimal;

/** Linha da tabela movimento: valor positivo = crédito, negativo = débito. */
public record Movimento(long contaId, BigDecimal valor, String descricao) {
}
