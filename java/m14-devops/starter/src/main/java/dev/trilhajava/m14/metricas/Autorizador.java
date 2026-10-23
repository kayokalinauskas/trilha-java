package dev.trilhajava.m14.metricas;

/**
 * O provedor de pagamentos: aprova ou recusa. Pode lançar exceção (fora do ar, timeout).
 * Nos testes, um mock.
 */
public interface Autorizador {

    boolean autorizar(Pagamento pagamento);
}
