package dev.trilhajava.m13.aulas.microsservicos.pedidos;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * O contrato do serviço de estoque, do ponto de vista do serviço de pedidos.
 * Os records abaixo são cópias locais: pedidos NÃO importa classes do estoque.
 * O que os dois compartilham é o formato do JSON, não o código.
 */
@HttpExchange
public interface EstoqueCliente {

    record PedidoDeReserva(String pedidoId, String sku, int quantidade) {}

    record ReservaConfirmada(String pedidoId, String sku, int quantidade) {}

    record Saldo(String sku, int disponivel, int reservado) {}

    @PostExchange("/reservas")
    ReservaConfirmada reservar(@RequestBody PedidoDeReserva pedido);

    @DeleteExchange("/reservas/{pedidoId}")
    void liberar(@PathVariable String pedidoId);

    @GetExchange(url = "/produtos/{sku}/saldo", version = "2")
    Saldo saldo(@PathVariable String sku);
}
