package dev.trilhajava.m04.vendas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 04.01 — Painel analítico de vendas com Streams")
class PainelVendasTest {

    private static Pedido p(String id, String cliente, String categoria, String data, String valor, StatusPedido status) {
        return new Pedido(id, cliente, categoria, LocalDate.parse(data), new BigDecimal(valor), status);
    }

    private final List<Pedido> pedidos = List.of(
            p("P1", "ana", "Livros", "2026-01-10", "120.00", StatusPedido.PAGO),
            p("P2", "bruno", "Eletrônicos", "2026-01-15", "2500.00", StatusPedido.PAGO),
            p("P3", "ana", "Livros", "2026-02-03", "80.00", StatusPedido.PAGO),
            p("P4", "carla", "Casa", "2026-02-20", "300.00", StatusPedido.CANCELADO),
            p("P5", "carla", "Eletrônicos", "2026-02-21", "999.90", StatusPedido.PAGO),
            p("P6", "davi", "Casa", "2026-03-01", "150.00", StatusPedido.PENDENTE),
            p("P7", "bruno", "Livros", "2026-03-05", "45.50", StatusPedido.PAGO));

    private final PainelVendas painel = new PainelVendas();

    @Test
    @DisplayName("faturamento por categoria considera só pedidos pagos, em ordem alfabética")
    void faturamentoPorCategoria() {
        Map<String, BigDecimal> r = painel.faturamentoPorCategoria(pedidos);
        assertThat(r.keySet()).containsExactly("Eletrônicos", "Livros");
        assertThat(r.get("Eletrônicos")).isEqualByComparingTo("3499.90");
        assertThat(r.get("Livros")).isEqualByComparingTo("245.50");
    }

    @Test
    @DisplayName("ticket médio dos pedidos pagos, com 2 casas")
    void ticketMedio() {
        assertThat(painel.ticketMedio(pedidos)).isEqualTo(new BigDecimal("749.08"));
        assertThat(painel.ticketMedio(List.of())).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("top clientes por valor pago; empate desempata pelo nome")
    void topClientes() {
        assertThat(painel.topClientes(pedidos, 2)).containsExactly("bruno", "carla");
        List<Pedido> empate = List.of(p("X", "zeca", "A", "2026-01-01", "10", StatusPedido.PAGO),
                p("Y", "alice", "A", "2026-01-01", "10", StatusPedido.PAGO));
        assertThat(painel.topClientes(empate, 5)).containsExactly("alice", "zeca");
    }

    @Test
    @DisplayName("contagem por status inclui todos os status, mesmo com zero")
    void porStatus() {
        Map<StatusPedido, Long> r = painel.contagemPorStatus(pedidos);
        assertThat(r).containsExactly(entry(StatusPedido.PENDENTE, 1L), entry(StatusPedido.PAGO, 5L), entry(StatusPedido.CANCELADO, 1L));
        assertThat(painel.contagemPorStatus(List.of())).containsEntry(StatusPedido.PAGO, 0L).hasSize(3);
    }

    @Test
    @DisplayName("faturamento mensal ordenado por mês")
    void mensal() {
        Map<YearMonth, BigDecimal> r = painel.faturamentoMensal(pedidos);
        assertThat(r.keySet()).containsExactly(YearMonth.of(2026, 1), YearMonth.of(2026, 2), YearMonth.of(2026, 3));
        assertThat(r.get(YearMonth.of(2026, 2))).isEqualByComparingTo("1079.90");
    }

    @Test
    @DisplayName("particiona ids dos pedidos pagos em acima/abaixo de um valor")
    void particao() {
        Map<Boolean, List<String>> r = painel.particionarPorValor(pedidos, new BigDecimal("500"));
        assertThat(r.get(true)).containsExactly("P2", "P5");
        assertThat(r.get(false)).containsExactly("P1", "P3", "P7");
    }

    @Test
    @DisplayName("clientes por categoria, sem repetição e ordenados")
    void clientesPorCategoria() {
        Map<String, List<String>> r = painel.clientesPorCategoria(pedidos);
        assertThat(r).containsEntry("Livros", List.of("ana", "bruno"))
                .containsEntry("Eletrônicos", List.of("bruno", "carla"))
                .doesNotContainKey("Casa");
    }

    @Test
    @DisplayName("não altera a lista recebida e devolve coleções imutáveis")
    void imutavel() {
        List<String> top = painel.topClientes(pedidos, 3);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> top.add("x")).isInstanceOf(UnsupportedOperationException.class);
    }
}
