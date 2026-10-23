package dev.trilhajava.m10.aulas.desempenho;

import java.math.BigDecimal;

/** Uma linha da tabela de frete: a região de uma faixa de CEPs e o valor base. */
public record FaixaDeFrete(String regiao, BigDecimal valorBase) {
}
