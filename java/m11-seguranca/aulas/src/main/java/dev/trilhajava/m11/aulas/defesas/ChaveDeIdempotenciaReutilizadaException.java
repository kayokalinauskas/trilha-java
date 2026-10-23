package dev.trilhajava.m11.aulas.defesas;

/** A mesma Idempotency-Key chegou com um corpo diferente do da primeira vez. */
public class ChaveDeIdempotenciaReutilizadaException extends RuntimeException {

    public ChaveDeIdempotenciaReutilizadaException(String chave) {
        super("A Idempotency-Key " + chave + " já foi usada com outro pedido. Gere uma chave nova para um pedido novo.");
    }
}
