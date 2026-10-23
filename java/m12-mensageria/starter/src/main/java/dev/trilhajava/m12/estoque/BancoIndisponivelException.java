package dev.trilhajava.m12.estoque;

/** Falha transitória (conexão caiu, timeout): tentar de novo pode dar certo. */
public class BancoIndisponivelException extends RuntimeException {

    public BancoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
