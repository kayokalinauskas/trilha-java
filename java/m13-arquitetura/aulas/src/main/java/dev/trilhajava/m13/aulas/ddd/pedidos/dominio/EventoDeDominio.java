package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.time.Instant;

/** Algo que aconteceu no domínio e interessa a outras partes do sistema. Nome no passado. */
public sealed interface EventoDeDominio permits PedidoConfirmado, PedidoCancelado {

    PedidoId pedido();

    Instant ocorridoEm();
}
