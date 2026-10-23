package dev.trilhajava.m13.aulas.resiliencia;

/** O JSON de {@code GET /elegibilidade/{carteirinha}}. */
public record RespostaDaOperadora(String carteirinha, String plano, boolean ativa) { }
