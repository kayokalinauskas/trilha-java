package dev.trilhajava.m04.vendas;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Exercício 04.01 — Painel analítico de vendas.
 * Rode: mvn test -Dtest=PainelVendasTest
 *
 * Restrição didática: resolva cada método com UM pipeline de Stream (sem for/while).
 * Salvo indicação contrária, só pedidos PAGOS entram nos cálculos de dinheiro.
 */
public class PainelVendas {

    /** Soma dos pedidos pagos por categoria, chaves em ordem alfabética. */
    public Map<String, BigDecimal> faturamentoPorCategoria(List<Pedido> pedidos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Média dos valores pagos, 2 casas (HALF_EVEN). Sem pedidos pagos: 0.00. */
    public BigDecimal ticketMedio(List<Pedido> pedidos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Os n clientes com maior soma paga; empate pelo nome (A→Z). Lista imutável. */
    public List<String> topClientes(List<Pedido> pedidos, int n) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Quantidade de pedidos por status (TODOS os pedidos), com todos os status presentes, na ordem do enum. */
    public Map<StatusPedido, Long> contagemPorStatus(List<Pedido> pedidos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Faturamento pago por mês (YearMonth), em ordem cronológica. */
    public Map<YearMonth, BigDecimal> faturamentoMensal(List<Pedido> pedidos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Ids dos pedidos pagos: true = valor >= limite; false = abaixo. Mantém a ordem original. */
    public Map<Boolean, List<String>> particionarPorValor(List<Pedido> pedidos, BigDecimal limite) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Para cada categoria com pedido pago: clientes distintos, em ordem alfabética. */
    public Map<String, List<String>> clientesPorCategoria(List<Pedido> pedidos) {
        throw new UnsupportedOperationException("TODO");
    }
}
