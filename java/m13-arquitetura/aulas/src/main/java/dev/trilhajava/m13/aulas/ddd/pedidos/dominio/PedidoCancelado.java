package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.time.Instant;

public record PedidoCancelado(PedidoId pedido, String motivo, Instant ocorridoEm) implements EventoDeDominio {
}
