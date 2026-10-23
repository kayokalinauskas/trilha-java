package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * O MESMO SQL no H2 (em "modo PostgreSQL") e num PostgreSQL 18 de verdade.
 * Sem Spring: só JUnit, Testcontainers e JDBC puro.
 */
@Testcontainers
@EnabledIfDockerAvailable
@DisplayName("H2 no modo PostgreSQL não é PostgreSQL")
class H2NaoEPostgresTest {

    @Container // static: um container para a classe inteira, parado no fim dela
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    Connection pg;
    Connection h2;

    @BeforeEach
    void conectarECriarTabela() throws SQLException {
        pg = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        h2 = DriverManager.getConnection("jdbc:h2:mem:comparacao;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
        for (Connection banco : List.of(pg, h2)) {
            executar(banco, "DROP TABLE IF EXISTS produto");
            executar(banco, "CREATE TABLE produto (sku VARCHAR(30) PRIMARY KEY, nome VARCHAR(60) NOT NULL, estoque INT)");
            executar(banco, """
                    INSERT INTO produto (sku, nome, estoque)
                    VALUES ('CAFE-1KG', 'café', 10), ('FILTRO-102', 'Filtro', NULL), ('ACUCAR-1KG', 'açúcar', 3)""");
        }
    }

    @AfterEach
    void desconectar() throws SQLException {
        pg.close();
        h2.close(); // o banco em memória do H2 some junto com a última conexão
    }

    @Test
    @DisplayName("upsert com ON CONFLICT ... DO UPDATE: o H2 nem reconhece a sintaxe")
    void upsert() throws SQLException {
        String upsert = """
                INSERT INTO produto (sku, nome, estoque) VALUES ('CAFE-1KG', 'café', 5)
                ON CONFLICT (sku) DO UPDATE SET estoque = produto.estoque + EXCLUDED.estoque""";

        executar(pg, upsert);
        assertThat(consultar(pg, "SELECT estoque FROM produto WHERE sku = 'CAFE-1KG'")).containsExactly("15");

        assertThatThrownBy(() -> executar(h2, upsert))
                .hasMessageContaining("Syntax error in SQL statement");
    }

    @Test
    @DisplayName("depois de um erro, o PostgreSQL aborta a transação; o H2 segue em frente")
    void transacaoAbortada() throws SQLException {
        String duplicado = "INSERT INTO produto (sku, nome, estoque) VALUES ('CAFE-1KG', 'café', 1)";
        String somar = "UPDATE produto SET estoque = estoque + 1 WHERE sku = 'CAFE-1KG'";
        pg.setAutoCommit(false);
        h2.setAutoCommit(false);

        assertThatThrownBy(() -> executar(h2, duplicado)).hasMessageContaining("Unique index or primary key violation");
        assertThat(executar(h2, somar)).isEqualTo(1); // o H2 aceita o próximo comando

        assertThatThrownBy(() -> executar(pg, duplicado)).hasMessageContaining("duplicate key value");
        assertThatThrownBy(() -> executar(pg, somar))
                .isInstanceOfSatisfying(SQLException.class, e -> assertThat(e.getSQLState()).isEqualTo("25P02"))
                .hasMessageContaining("current transaction is aborted");
    }

    @Test
    @DisplayName("ORDER BY em texto: o PostgreSQL usa a collation (en_US.utf8); o H2 compara códigos")
    void ordenacaoDeTexto() throws SQLException {
        String sql = "SELECT nome FROM produto ORDER BY nome";

        assertThat(consultar(pg, sql)).containsExactly("açúcar", "café", "Filtro");
        assertThat(consultar(h2, sql)).containsExactly("Filtro", "açúcar", "café");
    }

    @Test
    @DisplayName("ORDER BY com NULL: o PostgreSQL põe os nulos no fim; o H2, no começo")
    void nulosNaOrdenacao() throws SQLException {
        String sql = "SELECT sku FROM produto ORDER BY estoque";

        assertThat(consultar(pg, sql)).containsExactly("ACUCAR-1KG", "CAFE-1KG", "FILTRO-102");
        assertThat(consultar(h2, sql)).containsExactly("FILTRO-102", "ACUCAR-1KG", "CAFE-1KG");
    }

    @Test
    @DisplayName("jsonb e o operador ->>: só no PostgreSQL")
    void jsonb() throws SQLException {
        String sql = "SELECT CAST('{\"origem\": \"app\"}' AS jsonb) ->> 'origem'";

        assertThat(consultar(pg, sql)).containsExactly("app");
        assertThatThrownBy(() -> consultar(h2, sql)).hasMessageContaining("Syntax error in SQL statement");
    }

    @Test
    @DisplayName("índice parcial (CREATE INDEX ... WHERE): a migration nem roda no H2")
    void indiceParcial() throws SQLException {
        String ddl = "CREATE INDEX ix_produto_sem_estoque ON produto (sku) WHERE estoque = 0";

        executar(pg, ddl);
        assertThatThrownBy(() -> executar(h2, ddl)).hasMessageContaining("Syntax error in SQL statement");
    }

    // ---- utilitários de JDBC puro ----

    static int executar(Connection banco, String sql) throws SQLException {
        try (Statement comando = banco.createStatement()) {
            return comando.executeUpdate(sql);
        }
    }

    static List<String> consultar(Connection banco, String sql) throws SQLException {
        List<String> valores = new ArrayList<>();
        try (Statement comando = banco.createStatement(); ResultSet linhas = comando.executeQuery(sql)) {
            while (linhas.next()) {
                valores.add(linhas.getString(1));
            }
        }
        return valores;
    }
}
