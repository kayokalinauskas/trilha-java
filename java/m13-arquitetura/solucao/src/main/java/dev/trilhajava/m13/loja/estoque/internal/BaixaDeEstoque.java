package dev.trilhajava.m13.loja.estoque.internal;

import dev.trilhajava.m13.loja.pedidos.PedidoConfirmado;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Reage ao evento do módulo pedidos. @ApplicationModuleListener = assíncrono, numa transação
 * nova, e só depois do commit de quem publicou. O estoque depende do EVENTO de pedidos,
 * e pedidos não depende de nada do estoque.
 */
@Component
class BaixaDeEstoque {

    private final Saldos saldos;

    BaixaDeEstoque(Saldos saldos) {
        this.saldos = saldos;
    }

    @ApplicationModuleListener
    void quandoConfirmado(PedidoConfirmado evento) {
        evento.itens().forEach(item -> saldos.baixar(item.sku(), item.quantidade()));
    }
}
