package dev.trilhajava.m10.fila;

/** Uma tarefa da fila. payload é o JSON como texto. */
public record Tarefa(long id, String tipo, String payload, StatusTarefa status, int tentativas, String reservadaPor) {
}
