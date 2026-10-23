package dev.trilhajava.m12.pagamentos;

/**
 * Avisa o cliente (e-mail, push). É um efeito FORA do banco: não dá para desfazer num rollback.
 */
public interface NotificadorDePagamento {

    void pagamentoAprovado(String pedidoId);

    void pagamentoEstornado(String pedidoId);
}
