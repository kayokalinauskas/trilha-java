package dev.trilhajava.m10.aulas.reembolso;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Fake: guarda os pedidos num Map em vez de um banco. */
class RepositorioDePedidosEmMemoria implements RepositorioDePedidos {

    private final Map<String, Pedido> porNumero = new HashMap<>();

    RepositorioDePedidosEmMemoria com(Pedido pedido) {
        porNumero.put(pedido.numero(), pedido);
        return this;
    }

    @Override
    public Optional<Pedido> buscar(String numero) {
        return Optional.ofNullable(porNumero.get(numero));
    }
}
