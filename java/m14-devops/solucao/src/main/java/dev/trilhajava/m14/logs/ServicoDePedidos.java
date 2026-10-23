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
        // Campos estruturados (API fluente do SLF4J 2): viram chaves no JSON, e não texto no meio da mensagem.
        // O CPF só vai mascarado; o requestId vem do MDC, sem precisar ser passado por parâmetro.
        log.atInfo()
                .addKeyValue("pedido", pedido.id())
                .addKeyValue("valor", valor)
                .addKeyValue("cpf", Mascaras.cpf(cpf))
                .log("pedido criado");
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
