package dev.trilhajava.m10.aulas.desempenho;

import java.math.BigDecimal;

/** A resposta da API: quanto custa e em quantos dias úteis o pedido chega. */
public record Cotacao(String cep, String regiao, BigDecimal valor, int prazoDias) {
}
