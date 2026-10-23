package dev.trilhajava.m03.ranking;

import java.math.BigDecimal;

/** Uma posição do ranking. */
public record ItemRanking(String produto, int quantidade, BigDecimal faturamento) {
}
