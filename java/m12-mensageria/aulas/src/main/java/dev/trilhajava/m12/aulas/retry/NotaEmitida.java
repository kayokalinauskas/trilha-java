package dev.trilhajava.m12.aulas.retry;

/** O evento que chega em "notas.emitidas": a nota existe e precisa ser enviada ao e-mail do cliente. */
public record NotaEmitida(String numeroPedido, String email, String chaveDaNota) {
}
