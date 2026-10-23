package dev.trilhajava.m10.aulas.contrato.frete;

/**
 * A resposta de uma cotação. O valor vai como texto ("23.90"), e não como número JSON:
 * assim nenhum cliente o lê como double e perde centavos.
 */
public record Cotacao(String valor, int prazoEmDiasUteis, String modalidade, String transportadora) {
}
