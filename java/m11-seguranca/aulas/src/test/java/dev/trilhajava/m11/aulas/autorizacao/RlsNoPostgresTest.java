package dev.trilhajava.m11.aulas.autorizacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Defesa em profundidade: Row-Level Security no PostgreSQL 18. Mesmo uma consulta SEM "WHERE clinica_id"
 * só enxerga as linhas da clínica que a aplicação declarou na transação.
 */
@Testcontainers
@EnabledIfDockerAvailable
@DisplayName("RLS no PostgreSQL")
class RlsNoPostgresTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @BeforeAll
    static void criarEsquema() throws SQLException {
        try (Connection dono = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                Statement sql = dono.createStatement()) {
            sql.execute("""
                    CREATE TABLE prontuario (
                        id          bigint PRIMARY KEY,
                        clinica_id  text   NOT NULL,
                        paciente_id text   NOT NULL,
                        anotacoes   text
                    );
                    INSERT INTO prontuario VALUES
                        (1001, 'sol', 'pac-ana',   'Suspeita de enxaqueca'),
                        (1002, 'sol', 'pac-bruno', 'LDL 190 mg/dL'),
                        (1003, 'lua', 'pac-julia', 'Anti-histamínico');

                    ALTER TABLE prontuario ENABLE ROW LEVEL SECURITY;
                    CREATE POLICY isolamento_por_clinica ON prontuario
                        USING      (clinica_id = current_setting('app.clinica_id', true))
                        WITH CHECK (clinica_id = current_setting('app.clinica_id', true));

                    -- a aplicação conecta com um papel SEM superusuário e SEM ser dona da tabela
                    CREATE ROLE app_clinicas LOGIN PASSWORD 'senha-de-teste';
                    GRANT SELECT, INSERT, UPDATE ON prontuario TO app_clinicas;
                    """);
        }
    }

    static Connection comoAplicacao() throws SQLException {
        Connection conexao = DriverManager.getConnection(postgres.getJdbcUrl(), "app_clinicas", "senha-de-teste");
        conexao.setAutoCommit(false);
        return conexao;
    }

    /** O que a aplicação faz no início de cada transação: SET LOCAL do tenant, que some no commit. */
    static void declararClinica(Connection conexao, String clinicaId) throws SQLException {
        try (var comando = conexao.prepareStatement("SELECT set_config('app.clinica_id', ?, true)")) {
            comando.setString(1, clinicaId);
            comando.execute();
        }
    }

    static List<Long> idsVisiveis(Connection conexao) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (var consulta = conexao.createStatement();
                var linhas = consulta.executeQuery("SELECT id FROM prontuario ORDER BY id")) { // sem WHERE!
            while (linhas.next()) {
                ids.add(linhas.getLong("id"));
            }
        }
        return ids;
    }

    @Test
    @DisplayName("consulta sem WHERE só vê a clínica declarada na transação")
    void filtraPelaClinica() throws SQLException {
        try (Connection conexao = comoAplicacao()) {
            declararClinica(conexao, "sol");
            assertThat(idsVisiveis(conexao)).containsExactly(1001L, 1002L);
            conexao.commit();
        }
    }

    @Test
    @DisplayName("sem clínica declarada, não vê nada (falha fechada) — inclusive depois do commit")
    void semClinicaNaoVeNada() throws SQLException {
        try (Connection conexao = comoAplicacao()) {
            assertThat(idsVisiveis(conexao)).isEmpty();

            declararClinica(conexao, "lua");
            assertThat(idsVisiveis(conexao)).containsExactly(1003L);
            conexao.commit();                                  // o set_config(..., true) vale só na transação

            assertThat(idsVisiveis(conexao)).isEmpty();        // a conexão volta "limpa" para o pool
            conexao.commit();
        }
    }

    @Test
    @DisplayName("não grava linha de outra clínica (WITH CHECK)")
    void naoGravaEmOutraClinica() throws SQLException {
        try (Connection conexao = comoAplicacao()) {
            declararClinica(conexao, "lua");
            assertThatThrownBy(() -> {
                try (var insert = conexao.createStatement()) {
                    insert.executeUpdate("INSERT INTO prontuario VALUES (2001, 'sol', 'pac-ana', 'invasão')");
                }
            }).isInstanceOf(SQLException.class)
                    .hasMessageContaining("violates row-level security policy");
            conexao.rollback();
        }
    }

    @Test
    @DisplayName("UPDATE de linha de outra clínica afeta zero linhas, sem erro")
    void updateEmOutraClinicaNaoAfetaNada() throws SQLException {
        try (Connection conexao = comoAplicacao()) {
            declararClinica(conexao, "lua");
            try (var update = conexao.createStatement()) {
                int afetadas = update.executeUpdate("UPDATE prontuario SET anotacoes = 'alterado' WHERE id = 1001");
                assertThat(afetadas).isZero();
            }
            conexao.rollback();
        }
    }

    @Test
    @DisplayName("o dono da tabela (e superusuários) ignoram a política: por isso a aplicação usa outro papel")
    void donoIgnoraAPolitica() throws SQLException {
        try (Connection dono = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            assertThat(idsVisiveis(dono)).containsExactly(1001L, 1002L, 1003L);
        }
    }
}
