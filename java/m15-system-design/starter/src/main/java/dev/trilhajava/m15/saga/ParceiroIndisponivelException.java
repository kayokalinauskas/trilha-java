package dev.trilhajava.m15.saga;

/** Timeout ou erro de rede: não sabemos se o parceiro processou o pedido. */
public class ParceiroIndisponivelException extends RuntimeException {

    public ParceiroIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
