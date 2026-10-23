package dev.trilhajava.m12.aulas.idempotencia;

/** O que o consumidor fez com uma entrega. */
public enum Resultado {
    /** Primeira vez: o efeito foi aplicado. */
    PROCESSADO,
    /** Repetição (da mesma mensagem ou da mesma compra): nada mudou. */
    DUPLICADO
}
