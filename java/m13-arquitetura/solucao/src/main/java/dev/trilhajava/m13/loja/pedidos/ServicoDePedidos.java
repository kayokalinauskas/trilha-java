package dev.trilhajava.m13.loja.pedidos;

import dev.trilhajava.m13.loja.pedidos.internal.Pedidos;
import dev.trilhajava.m13.loja.pedidos.internal.Pedidos.Pedido;
import dev.trilhajava.m13.loja.pedidos.internal.Pedidos.Status;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A API pública do módulo pedidos. Confirmar um pedido não chama o estoque nem o faturamento:
 * publica um fato (PedidoConfirmado) e segue. Quem se interessa, escuta.
 */
@Service
public class ServicoDePedidos {

    private final Pedidos pedidos;
    private final ApplicationEventPublisher eventos;

    ServicoDePedidos(Pedidos pedidos, ApplicationEventPublisher eventos) {
        this.pedidos = pedidos;
        this.eventos = eventos;
    }

    public UUID criar(String cliente, List<Item> itens) {
        return pedidos.salvar(new Pedido(UUID.randomUUID(), cliente, List.copyOf(itens), Status.CRIADO)).id();
    }

    /**
     * O evento é publicado dentro da transação: o registro de publicações do Spring Modulith o
     * grava na mesma transação, e os listeners dos outros módulos só rodam depois do commit.
     */
    @Transactional
    public void confirmar(UUID id) {
        Pedido pedido = pedidos.buscar(id).orElseThrow(() -> new PedidoNaoEncontradoException(id));
        if (pedido.status() == Status.CONFIRMADO) {
            return;
        }
        pedidos.salvar(new Pedido(pedido.id(), pedido.cliente(), pedido.itens(), Status.CONFIRMADO));
        BigDecimal total = pedido.itens().stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        eventos.publishEvent(new PedidoConfirmado(pedido.id(), pedido.cliente(), pedido.itens(), total));
    }

    public boolean confirmado(UUID id) {
        return pedidos.buscar(id).map(p -> p.status() == Status.CONFIRMADO).orElse(false);
    }
}
