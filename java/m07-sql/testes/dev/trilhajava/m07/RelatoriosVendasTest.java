package dev.trilhajava.m07;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 07.02 — Relatórios de vendas (JOIN, GROUP BY, HAVING)")
class RelatoriosVendasTest {

    private Banco banco;

    @BeforeEach
    void criar() throws SQLException {
        banco = new Banco();
        banco.executar(DadosVendas.SCRIPT);
    }

    @AfterEach
    void fechar() throws SQLException {
        banco.close();
    }

    @Test
    @DisplayName("q1: faturamento por categoria, só pedidos PAGO, do maior para o menor")
    void q1() throws SQLException {
        assertThat(banco.nomesDasColunas("sql/0702/q1-faturamento-por-categoria.sql")).containsExactly("categoria", "faturamento");
        assertThat(banco.consultarArquivo("sql/0702/q1-faturamento-por-categoria.sql")).containsExactly(
                List.of("Eletro", "850"), List.of("Mercearia", "205"), List.of("Casa", "160"));
    }

    @Test
    @DisplayName("q2: clientes que nunca fizeram pedido")
    void q2() throws SQLException {
        assertThat(banco.consultarArquivo("sql/0702/q2-clientes-sem-pedido.sql")).containsExactly(List.of("Fábio"));
    }

    @Test
    @DisplayName("q3: clientes com 2 ou mais pedidos pagos")
    void q3() throws SQLException {
        assertThat(banco.nomesDasColunas("sql/0702/q3-clientes-recorrentes.sql")).containsExactly("nome", "pedidos_pagos");
        assertThat(banco.consultarArquivo("sql/0702/q3-clientes-recorrentes.sql")).containsExactly(
                List.of("Ana", "3"), List.of("Carla", "3"));
    }

    @Test
    @DisplayName("q4: ticket médio por cidade (pedidos pagos), 2 casas")
    void q4() throws SQLException {
        assertThat(banco.consultarArquivo("sql/0702/q4-ticket-medio-por-cidade.sql")).containsExactly(
                List.of("Curitiba", "360"), List.of("Recife", "103.33"), List.of("São Paulo", "136.25"));
    }

    @Test
    @DisplayName("q5: produtos que nunca foram vendidos em pedido PAGO")
    void q5() throws SQLException {
        assertThat(banco.consultarArquivo("sql/0702/q5-produtos-parados.sql")).containsExactly(List.of("Bule"));
    }
}
