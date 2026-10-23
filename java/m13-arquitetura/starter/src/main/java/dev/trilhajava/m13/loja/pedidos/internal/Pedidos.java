package dev.trilhajava.m13.loja.pedidos.internal;

import dev.trilhajava.m13.loja.pedidos.Item;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Detalhe interno do módulo pedidos: nenhum outro módulo pode usar esta classe. */
@Component
public class Pedidos {

    public enum Status { CRIADO, CONFIRMADO }

    public record Pedido(UUID id, String cliente, List<Item> itens, Status status) {
    }

    private final Map<UUID, Pedido> pedidos = new ConcurrentHashMap<>();

    public Pedido salvar(Pedido pedido) {
        pedidos.put(pedido.id(), pedido);
        return pedido;
    }

    public Optional<Pedido> buscar(UUID id) {
        return Optional.ofNullable(pedidos.get(id));
    }
}
