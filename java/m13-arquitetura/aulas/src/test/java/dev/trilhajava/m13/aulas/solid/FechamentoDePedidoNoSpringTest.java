package dev.trilhajava.m13.aulas.solid;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** O Spring monta o grafo: injeta todas as regras na calculadora e os adaptadores nas portas. */
class FechamentoDePedidoNoSpringTest {

    private static Carrinho carrinhoVipComAtacado() {
        return new Carrinho("cliente-42", TipoDeCliente.VIP, List.of(
                new ItemDoPedido("CAFE-1KG", 12, new BigDecimal("49.90")),
                new ItemDoPedido("FILTRO-102", 1, new BigDecimal("12.50"))), null);
    }

    @Test
    void injetaTodasAsRegrasEFechaOPedido() {
        try (var contexto = new AnnotationConfigApplicationContext(ConfiguracaoDaLoja.class)) {
            var fechamento = contexto.getBean(FechamentoDePedido.class);

            PedidoFechado pedido = fechamento.fechar("PED-1", carrinhoVipComAtacado());

            assertThat(pedido.orcamento().descontos()).extracting(Desconto::motivo)
                    .containsExactlyInAnyOrder("Cliente VIP (5%)", "Atacado (3% por item com 10+ unidades)");
            assertThat(pedido.orcamento().total()).isEqualByComparingTo("562.77");
            assertThat(contexto.getBean(RepositorioDePedidos.class).buscar("PED-1")).contains(pedido);
        }
    }

    @Test
    void regraNovaEntraSemEditarACalculadora() {
        try (var contexto = new AnnotationConfigApplicationContext()) {
            contexto.register(ConfiguracaoDaLoja.class);
            contexto.registerBean("descontoBlackFriday", RegraDeDesconto.class,
                    () -> carrinho -> Optional.of(new Desconto("Black Friday", new BigDecimal("10.00"))));
            contexto.refresh();

            Orcamento orcamento = contexto.getBean(CalculadoraDeDescontos.class).orcar(carrinhoVipComAtacado());

            assertThat(orcamento.descontos()).hasSize(3);
            assertThat(orcamento.total()).isEqualByComparingTo("552.77");
        }
    }
}
