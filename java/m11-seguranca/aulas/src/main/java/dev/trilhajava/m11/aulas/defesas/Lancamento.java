package dev.trilhajava.m11.aulas.defesas;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Uma linha do extrato. Valor negativo é saída; positivo, entrada. */
public record Lancamento(LocalDate data, String descricao, BigDecimal valor) {
}
