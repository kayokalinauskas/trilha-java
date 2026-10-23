package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.time.Instant;

public record PedidoConfirmado(PedidoId pedido, ClienteId cliente, Dinheiro total, Instant ocorridoEm)
        implements EventoDeDominio {
}
