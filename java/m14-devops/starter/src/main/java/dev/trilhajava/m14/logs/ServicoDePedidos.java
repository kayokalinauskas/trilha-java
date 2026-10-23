package dev.trilhajava.m14.logs;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ServicoDePedidos {

    public record Pedido(UUID id, String cliente, BigDecimal valor) {
    }

    private static final Logger log = LoggerFactory.getLogger(ServicoDePedidos.class);

    private final Map<UUID, Pedido> pedidos = new ConcurrentHashMap<>();

    public Pedido criar(String cliente, String cpf, BigDecimal valor) {
        var pedido = new Pedido(UUID.randomUUID(), cliente, valor);
        pedidos.put(pedido.id(), pedido);
        // TODO: este log tem dois problemas. Os dados estão presos no texto (ninguém consegue
        //  filtrar por pedido ou somar valores) e o CPF vaza inteiro. Use campos estruturados
        //  (pedido, valor, cpf mascarado com Mascaras.cpf) e a mensagem fixa "pedido criado".
        log.info("pedido {} criado para o CPF {} no valor de {}", pedido.id(), cpf, valor);
        return pedido;
    }

    public Pedido buscar(UUID id) {
        Pedido pedido = pedidos.get(id);
        if (pedido == null) {
            throw new PedidoNaoEncontradoException(id);
        }
        return pedido;
    }
}
