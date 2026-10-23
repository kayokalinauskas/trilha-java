package dev.trilhajava.m13.loja.pedidos;

import dev.trilhajava.m13.loja.estoque.internal.Saldos;
import dev.trilhajava.m13.loja.faturamento.internal.EmissaoDeFaturas;
import dev.trilhajava.m13.loja.pedidos.internal.Pedidos;
import dev.trilhajava.m13.loja.pedidos.internal.Pedidos.Pedido;
import dev.trilhajava.m13.loja.pedidos.internal.Pedidos.Status;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A versão que está em produção: confirmar um pedido chama o estoque e o faturamento DIRETAMENTE,
 * inclusive classes internas deles. Funciona... até a SEFAZ cair e ninguém conseguir comprar.
 *
 * <p>Exercício 13.03 — refatore aqui: pedidos publica um fato, e estoque e faturamento reagem.
 * Os requisitos e as regras estão no enunciado, no site.
 */
@Service
public class ServicoDePedidos {

    private final Pedidos pedidos;
    private final Saldos saldos;
    private final EmissaoDeFaturas faturamento;

    ServicoDePedidos(Pedidos pedidos, Saldos saldos, EmissaoDeFaturas faturamento) {
        this.pedidos = pedidos;
        this.saldos = saldos;
        this.faturamento = faturamento;
    }

    public UUID criar(String cliente, List<Item> itens) {
        return pedidos.salvar(new Pedido(UUID.randomUUID(), cliente, List.copyOf(itens), Status.CRIADO)).id();
    }

    @Transactional
    public void confirmar(UUID id) {
        Pedido pedido = pedidos.buscar(id).orElseThrow(() -> new PedidoNaoEncontradoException(id));
        if (pedido.status() == Status.CONFIRMADO) {
            return;
        }
        pedidos.salvar(new Pedido(pedido.id(), pedido.cliente(), pedido.itens(), Status.CONFIRMADO));
        pedido.itens().forEach(item -> saldos.baixar(item.sku(), item.quantidade()));
        BigDecimal total = pedido.itens().stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        faturamento.emitir(pedido.id(), pedido.cliente(), total);
    }

    public boolean confirmado(UUID id) {
        return pedidos.buscar(id).map(p -> p.status() == Status.CONFIRMADO).orElse(false);
    }
}
