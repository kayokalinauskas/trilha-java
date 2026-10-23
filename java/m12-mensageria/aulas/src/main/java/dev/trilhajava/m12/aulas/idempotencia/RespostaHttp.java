package dev.trilhajava.m12.aulas.idempotencia;

/** O status e o corpo que a API devolveu (e que a repetição precisa devolver iguais). */
public record RespostaHttp(int status, String corpo) {
}
