package dev.trilhajava.m13.aulas.ddd.pedidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.ClienteId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Dinheiro;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.ItemDoPedido;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Pedido;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.PedidoConfirmado;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.PedidoId;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.RegraDoPedidoException;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.Sku;
import dev.trilhajava.m13.aulas.ddd.pedidos.dominio.StatusDoPedido;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** O agregado testado sozinho: sem Spring, sem banco, sem mocks. */
class PedidoTest {

    static final Instant AGORA = Instant.parse("2026-10-06T13:00:00Z");
    final Pedido pedido = Pedido.abrir(PedidoId.novo(), new ClienteId("CLI-1"));

    @Test
    void somaOMesmoSkuEConfirmaComUmEvento() {
        pedido.adicionarItem(new Sku("CAFE-1KG"), 2, Dinheiro.de("49.90"));
        pedido.adicionarItem(new Sku("FILTRO-102"), 1, Dinheiro.de("12.50"));
        pedido.adicionarItem(new Sku("CAFE-1KG"), 1, Dinheiro.de("49.9"));

        pedido.confirmar(AGORA);

        assertThat(pedido.itens()).containsExactly(
                new ItemDoPedido(new Sku("CAFE-1KG"), 3, Dinheiro.de("49.90")),
                new ItemDoPedido(new Sku("FILTRO-102"), 1, Dinheiro.de("12.50")));
        assertThat(pedido.retirarEventos()).containsExactly(
                new PedidoConfirmado(pedido.id(), new ClienteId("CLI-1"), Dinheiro.de("162.20"), AGORA));
        assertThat(pedido.retirarEventos()).isEmpty();          // cada evento sai uma vez só
    }

    @Test
    void naoAceitaItensDepoisDeConfirmado() {
        pedido.adicionarItem(new Sku("CAFE-1KG"), 1, Dinheiro.de("49.90"));
        pedido.confirmar(AGORA);

        assertThatThrownBy(() -> pedido.adicionarItem(new Sku("CANECA"), 1, Dinheiro.de("35.00")))
                .isInstanceOf(RegraDoPedidoException.class)
                .hasMessageContaining("está CONFIRMADO");
        assertThat(pedido.itens()).hasSize(1);
        assertThat(pedido.status()).isEqualTo(StatusDoPedido.CONFIRMADO);
    }

    @Test
    void recusaPassarDoLimiteSemMudarOEstado() {
        pedido.adicionarItem(new Sku("NOTEBOOK-PRO"), 1, Dinheiro.de("49999.00"));

        assertThatThrownBy(() -> pedido.adicionarItem(new Sku("CABO-USB"), 2, Dinheiro.de("1.00")))
                .isInstanceOf(RegraDoPedidoException.class)
                .hasMessage("O total ficaria em R$ 50001.00, acima do limite de R$ 50000.00");
        assertThat(pedido.total()).isEqualTo(Dinheiro.de("49999.00"));
        assertThat(pedido.itens()).hasSize(1);
    }
}
