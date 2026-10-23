package dev.trilhajava.m10.aulas.fatias;

import java.math.BigDecimal;

/** Projeção da consulta de relatório: quanto dinheiro está parado no estoque de cada categoria. */
public record ValorPorCategoria(String categoria, BigDecimal valor) {
}
