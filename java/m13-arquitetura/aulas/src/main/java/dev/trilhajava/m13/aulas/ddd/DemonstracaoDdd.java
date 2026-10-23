package dev.trilhajava.m13.aulas.ddd;

import dev.trilhajava.m13.aulas.ddd.financeiro.FichaFinanceira;
import dev.trilhajava.m13.aulas.ddd.financeiro.SistemaFinanceiro;
import dev.trilhajava.m13.aulas.ddd.pedidos.adaptador.CreditoViaFinanceiro;
import dev.trilhajava.m13.aulas.ddd.pedidos.adaptador.EventosEmMemoria;
import dev.trilhajava.m13.aulas.ddd.pedidos.adaptador.PedidosEmMemoria;
import dev.trilhajava.m13.aulas.ddd.pedidos.aplicacao.ConfirmarPedido;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.ClienteId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Dinheiro;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Pedido;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.PedidoId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.RegraDoPedidoException;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Sku;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/** Monta o contexto de Vendas com adaptadores em memória e percorre três cenários. */
public final class DemonstracaoDdd {

    public static void main(String[] args) {
        Clock relogio = Clock.fixed(Instant.parse("2026-10-06T13:00:00Z"), ZoneOffset.UTC);
        SistemaFinanceiro financeiro = codCli -> switch (codCli) {
            case "CLI-1" -> new FichaFinanceira("CLI-1", 500_000, 120_000, "A");   // limite R$ 5.000, em aberto R$ 1.200
            case "CLI-2" -> new FichaFinanceira("CLI-2", 1_000_000, 0, "B");       // bloqueado
            default -> null;
        };
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        EventosEmMemoria eventos = new EventosEmMemoria();
        ConfirmarPedido confirmarPedido =
                new ConfirmarPedido(pedidos, new CreditoViaFinanceiro(financeiro), eventos, relogio);

        System.out.println("== 1. Montando e confirmando um pedido");
        Pedido pedido = Pedido.abrir(
                new PedidoId(UUID.fromString("7d1c2f9e-3a4b-4c5d-8e6f-0a1b2c3d4e5f")), new ClienteId("CLI-1"));
        pedido.adicionarItem(new Sku("CAFE-1KG"), 2, Dinheiro.de("49.90"));
        pedido.adicionarItem(new Sku("FILTRO-102"), 1, Dinheiro.de("12.50"));
        pedido.adicionarItem(new Sku("CAFE-1KG"), 1, Dinheiro.de("49.90"));
        pedidos.salvar(pedido);
        pedido.itens().forEach(item -> System.out.println("Item: " + item));
        System.out.println("Total: " + pedido.total());
        confirmarPedido.executar(pedido.id());
        System.out.println("Status: " + pedido.status());
        eventos.publicados().forEach(evento -> System.out.println("Evento: " + evento));

        System.out.println("== 2. O agregado protege as invariantes");
        try {
            pedido.adicionarItem(new Sku("CANECA"), 1, Dinheiro.de("35.00"));
        } catch (RegraDoPedidoException e) {
            System.out.println("Recusado: " + e.getMessage());
        }
        System.out.println("Itens continuam: " + pedido.itens().size() + ", total " + pedido.total());

        System.out.println("== 3. A ACL traduz o Financeiro");
        Pedido outro = Pedido.abrir(
                new PedidoId(UUID.fromString("0b9e8d7c-6f5a-4b3c-9d2e-1f0a9b8c7d6e")), new ClienteId("CLI-2"));
        outro.adicionarItem(new Sku("CAFE-1KG"), 1, Dinheiro.de("49.90"));
        pedidos.salvar(outro);
        try {
            confirmarPedido.executar(outro.id());
        } catch (RegraDoPedidoException e) {
            System.out.println("Recusado: " + e.getMessage());
        }
        System.out.println("Status: " + outro.status() + "; eventos publicados no total: " + eventos.publicados().size());
    }
}
