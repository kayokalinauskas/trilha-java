package dev.trilhajava.m13.aulas.ddd.pedidos.adaptador;

import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Pedido;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.PedidoId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.RepositorioDePedidos;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Adaptador de saída em memória, para a demonstração e os testes. Em produção: JPA ou JDBC. */
public final class PedidosEmMemoria implements RepositorioDePedidos {

    private final Map<PedidoId, Pedido> porId = new HashMap<>();

    @Override
    public Optional<Pedido> porId(PedidoId id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public void salvar(Pedido pedido) {
        porId.put(pedido.id(), pedido);
    }
}
