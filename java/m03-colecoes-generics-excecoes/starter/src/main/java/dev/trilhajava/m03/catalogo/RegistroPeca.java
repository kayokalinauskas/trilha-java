package dev.trilhajava.m03.catalogo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma linha "crua" vinda da exportação de um sistema. Pode ter dados faltando ou inválidos.
 * Já pronto: repare que ele NÃO valida nada — é só transporte de dados.
 */
public record RegistroPeca(String codigo, String nome, BigDecimal preco, LocalDate atualizadoEm) {
}
