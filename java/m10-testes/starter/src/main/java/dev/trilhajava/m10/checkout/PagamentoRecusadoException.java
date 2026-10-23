package dev.trilhajava.m10.checkout;

public class PagamentoRecusadoException extends RuntimeException {

    public PagamentoRecusadoException(String motivo) {
        super("Pagamento recusado: " + motivo);
    }
}
