package dev.trilhajava.m10.aulas.reembolso;

import java.util.Optional;

/** Onde os pedidos ficam guardados (na aplicação real, um banco de dados). */
public interface RepositorioDePedidos {

    Optional<Pedido> buscar(String numero);
}
