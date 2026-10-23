package dev.trilhajava.m13.aulas.solid.adaptadores;

import dev.trilhajava.m13.aulas.solid.PedidoFechado;
import dev.trilhajava.m13.aulas.solid.RepositorioDePedidos;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Adaptador simples da porta. Em produção, seria um adaptador JDBC ou JPA com o mesmo contrato. */
@Repository
public class RepositorioDePedidosEmMemoria implements RepositorioDePedidos {

    private final Map<String, PedidoFechado> porNumero = new ConcurrentHashMap<>();

    @Override
    public void salvar(PedidoFechado pedido) {
        porNumero.put(pedido.numero(), pedido);
    }

    @Override
    public Optional<PedidoFechado> buscar(String numero) {
        return Optional.ofNullable(porNumero.get(numero));
    }
}
