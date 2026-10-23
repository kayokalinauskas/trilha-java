package dev.trilhajava.m12.outbox;

/**
 * O que uma execução do relay fez.
 *
 * @param publicados  eventos publicados com sucesso
 * @param falhas      tentativas que falharam (inclusive as que tornaram o evento MORTO)
 * @param adiados     eventos pendentes que ficaram para depois (antes da hora, ou atrás de outro do mesmo agregado)
 */
public record Resultado(int publicados, int falhas, int adiados) {
}
