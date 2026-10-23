package dev.trilhajava.m13.aulas.resiliencia;

/** Falha TRANSITÓRIA: 5xx, timeout, conexão recusada. Vale tentar de novo (se a operação for idempotente). */
public class OperadoraIndisponivelException extends RuntimeException {

    public OperadoraIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
