package dev.trilhajava.m13.aulas.ddd.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m13.aulas.ddd.financeiro.FichaFinanceira;
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
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.StatusDoPedido;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** O caso de uso com fakes: o Financeiro é uma lambda, a ACL é a real. */
class ConfirmarPedidoTest {

    final Clock relogio = Clock.fixed(Instant.parse("2026-10-06T13:00:00Z"), ZoneOffset.UTC);
    final PedidosEmMemoria pedidos = new PedidosEmMemoria();
    final EventosEmMemoria eventos = new EventosEmMemoria();

    @Test
    void creditoInsuficienteNaoConfirmaNemPublica() {
        // limite R$ 300,00 com R$ 250,00 em aberto: sobram R$ 50,00
        var acl = new CreditoViaFinanceiro(cod -> new FichaFinanceira(cod, 30_000, 25_000, "A"));
        var confirmarPedido = new ConfirmarPedido(pedidos, acl, eventos, relogio);
        Pedido pedido = Pedido.abrir(PedidoId.novo(), new ClienteId("CLI-9"));
        pedido.adicionarItem(new Sku("CAFE-1KG"), 2, Dinheiro.de("49.90"));
        pedidos.salvar(pedido);

        assertThatThrownBy(() -> confirmarPedido.executar(pedido.id()))
                .isInstanceOf(RegraDoPedidoException.class)
                .hasMessageEndingWith("total de R$ 99.80 acima do crédito disponível de R$ 50.00");
        assertThat(pedido.status()).isEqualTo(StatusDoPedido.RASCUNHO);
        assertThat(eventos.publicados()).isEmpty();
    }

    @Test
    void aclRecusaSituacaoQueNaoConhece() {
        var acl = new CreditoViaFinanceiro(cod -> new FichaFinanceira(cod, 100_000, 0, "X"));

        assertThatThrownBy(() -> acl.situacaoDe(new ClienteId("CLI-7")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Situação desconhecida no Financeiro para o cliente CLI-7: X");
    }
}
