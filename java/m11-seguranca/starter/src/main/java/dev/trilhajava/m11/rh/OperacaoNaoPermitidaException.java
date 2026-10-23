package dev.trilhajava.m11.rh;

/** 403: quem pede pode VER o recurso, mas não pode fazer esta operação nele. */
public class OperacaoNaoPermitidaException extends RuntimeException {

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
