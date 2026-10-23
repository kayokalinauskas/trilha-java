package dev.trilhajava.m11.aulas.autorizacao;

/** Vira 403: a pessoa vê o prontuário, mas não pode fazer esta operação nele. */
public class OperacaoNegadaException extends RuntimeException {

    public OperacaoNegadaException(String mensagem) {
        super(mensagem);
    }
}
