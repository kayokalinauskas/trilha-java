package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.util.Optional;

/**
 * Um repositório por agregado: carrega e salva o Pedido inteiro, com os itens.
 * Não existe RepositorioDeItens, porque item não vive fora do pedido.
 */
public interface RepositorioDePedidos {

    Optional<Pedido> porId(PedidoId id);

    void salvar(Pedido pedido);
}
