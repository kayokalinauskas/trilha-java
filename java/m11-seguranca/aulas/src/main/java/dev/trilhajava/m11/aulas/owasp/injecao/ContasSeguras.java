package dev.trilhajava.m11.aulas.owasp.injecao;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;

/** A versão corrigida: valores por parâmetro de bind, identificadores por allowlist. */
public class ContasSeguras {

    private final JdbcClient jdbc;

    public ContasSeguras(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Conta> buscarPorTitular(String agencia, String titular) {
        return jdbc.sql("SELECT id, titular, agencia, saldo FROM conta"
                        + " WHERE agencia = :agencia AND titular = :titular")
                .param("agencia", agencia)
                .param("titular", titular)
                .query(Conta.class)
                .list();
    }

    public List<Conta> listar(String agencia, String ordenarPor) {
        String ordem = OrdenacaoDeContas.paraSql(ordenarPor); // lança antes de tocar no banco
        return jdbc.sql("SELECT id, titular, agencia, saldo FROM conta WHERE agencia = :agencia"
                        + " ORDER BY " + ordem)
                .param("agencia", agencia)
                .query(Conta.class)
                .list();
    }
}
