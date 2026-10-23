package dev.trilhajava.m11.aulas.owasp.injecao;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * VULNERÁVEL DE PROPÓSITO (A05 Injection). Não copie.
 * Monta o SQL concatenando texto que veio da requisição.
 */
public class ContasVulneraveis {

    private final JdbcClient jdbc;

    public ContasVulneraveis(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Busca as contas de uma agência pelo nome do titular. */
    public List<Conta> buscarPorTitular(String agencia, String titular) {
        String sql = "SELECT id, titular, agencia, saldo FROM conta"
                + " WHERE agencia = '" + agencia + "' AND titular = '" + titular + "'";
        return jdbc.sql(sql).query(Conta.class).list();
    }

    /** Lista as contas de uma agência ordenadas pelo campo que o cliente pediu (?ordenarPor=saldo). */
    public List<Conta> listar(String agencia, String ordenarPor) {
        return jdbc.sql("SELECT id, titular, agencia, saldo FROM conta WHERE agencia = :agencia"
                        + " ORDER BY " + ordenarPor)
                .param("agencia", agencia)
                .query(Conta.class)
                .list();
    }
}
