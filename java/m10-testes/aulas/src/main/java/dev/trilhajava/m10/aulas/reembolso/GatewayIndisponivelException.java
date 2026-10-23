package dev.trilhajava.m10.aulas.reembolso;

/** O gateway de pagamento não respondeu. */
public class GatewayIndisponivelException extends RuntimeException {

    public GatewayIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
