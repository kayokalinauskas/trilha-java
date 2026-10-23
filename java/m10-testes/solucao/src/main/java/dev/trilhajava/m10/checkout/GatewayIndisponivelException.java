package dev.trilhajava.m10.checkout;

public class GatewayIndisponivelException extends RuntimeException {

    public GatewayIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
