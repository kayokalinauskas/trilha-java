package dev.trilhajava.m11.aulas.defesas;

import java.time.Duration;

/**
 * Uma cota: no máximo {@code capacidade} créditos por {@code janela}, com rajada de até
 * {@code capacidade} de uma vez. Ex.: ("por-cliente", 60, 1 minuto) repõe 1 crédito por segundo.
 */
public record PoliticaDeLimite(String nome, int capacidade, Duration janela) {

    public PoliticaDeLimite {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("a política precisa de um nome");
        }
        if (capacidade <= 0) {
            throw new IllegalArgumentException("capacidade deve ser positiva: " + capacidade);
        }
        if (janela == null || janela.isNegative() || janela.isZero()) {
            throw new IllegalArgumentException("janela deve ser positiva: " + janela);
        }
        if (janela.dividedBy(capacidade).isZero()) {
            throw new IllegalArgumentException("janela curta demais para " + capacidade + " créditos");
        }
    }

    /** O tempo que repõe UM crédito (60 por minuto → 1 s). */
    public Duration intervaloPorCredito() {
        return janela.dividedBy(capacidade);
    }

    /** O tempo para encher o balde vazio: capacidade × intervalo (sem resto de divisão). */
    public Duration tempoParaEncher() {
        return intervaloPorCredito().multipliedBy(capacidade);
    }
}
