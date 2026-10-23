package dev.trilhajava.m10.checkout;

public interface Notificador {

    void pedidoConfirmado(String clienteId, String numeroDoPedido);

    void pagamentoRecusado(String clienteId, String motivo);
}
