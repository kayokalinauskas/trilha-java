package dev.trilhajava.m07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 07.01 — Modelagem do banco de uma clínica (DDL e constraints)")
class ModelagemClinicaTest {

    private Banco banco;

    @BeforeEach
    void criar() throws SQLException {
        banco = new Banco();
        banco.executarArquivo("sql/0701/schema.sql");
        banco.executar("""
                INSERT INTO paciente (cpf, nome, nascimento, email) VALUES ('52998224725', 'Ana Souza', DATE '1990-05-10', 'ana@email.com');
                INSERT INTO medico (crm, nome, especialidade) VALUES ('SP-123456', 'Dr. Bruno', 'Cardiologia');
                """);
    }

    @AfterEach
    void fechar() throws SQLException {
        banco.close();
    }

    private void consulta(String inicio, int duracao, String status, String valor) throws SQLException {
        banco.executar("INSERT INTO consulta (paciente_id, medico_id, inicio, duracao_min, status, valor) VALUES ("
                + "(SELECT id FROM paciente WHERE cpf = '52998224725'), (SELECT id FROM medico WHERE crm = 'SP-123456'), "
                + "TIMESTAMP '" + inicio + "', " + duracao + ", " + (status == null ? "DEFAULT" : "'" + status + "'") + ", " + valor + ")");
    }

    @Test
    @DisplayName("aceita dados válidos, gera ids e aplica o status padrão AGENDADA")
    void validos() throws SQLException {
        consulta("2026-10-01 09:00:00", 30, null, "250.00");
        assertThat(banco.consultar("SELECT status, valor FROM consulta")).containsExactly(java.util.List.of("AGENDADA", "250"));
        assertThat(banco.consultar("SELECT id FROM paciente")).hasSize(1).allSatisfy(l -> assertThat(l.getFirst()).isNotNull());
    }

    @Nested
    @DisplayName("paciente")
    class Paciente {
        @Test
        @DisplayName("CPF é obrigatório, único e tem 11 dígitos")
        void cpf() {
            assertThatThrownBy(() -> banco.executar("INSERT INTO paciente (cpf, nome, nascimento) VALUES ('52998224725', 'Outra', DATE '2000-01-01')"))
                    .as("duplicado").isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> banco.executar("INSERT INTO paciente (cpf, nome, nascimento) VALUES (NULL, 'X', DATE '2000-01-01')"))
                    .as("nulo").isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> banco.executar("INSERT INTO paciente (cpf, nome, nascimento) VALUES ('123', 'X', DATE '2000-01-01')"))
                    .as("tamanho").isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("nome e nascimento são obrigatórios; e-mail é opcional mas único")
        void campos() throws SQLException {
            assertThatThrownBy(() -> banco.executar("INSERT INTO paciente (cpf, nascimento) VALUES ('11144477735', DATE '2000-01-01')"))
                    .isInstanceOf(SQLException.class);
            banco.executar("INSERT INTO paciente (cpf, nome, nascimento) VALUES ('11144477735', 'Sem Email', DATE '2000-01-01')");
            banco.executar("INSERT INTO paciente (cpf, nome, nascimento) VALUES ('00000000191', 'Sem Email 2', DATE '2000-01-01')");
            assertThatThrownBy(() -> banco.executar(
                    "INSERT INTO paciente (cpf, nome, nascimento, email) VALUES ('39053344705', 'X', DATE '2000-01-01', 'ana@email.com')"))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Nested
    @DisplayName("consulta")
    class Consulta {
        @Test
        @DisplayName("duração entre 15 e 120 minutos")
        void duracao() {
            assertThatThrownBy(() -> consulta("2026-10-01 09:00:00", 10, null, "100")).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> consulta("2026-10-01 09:00:00", 121, null, "100")).isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("status só aceita AGENDADA, REALIZADA ou CANCELADA")
        void status() {
            assertThatThrownBy(() -> consulta("2026-10-01 09:00:00", 30, "PERDIDA", "100")).isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("valor não pode ser negativo")
        void valor() {
            assertThatThrownBy(() -> consulta("2026-10-01 09:00:00", 30, null, "-1")).isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("o mesmo médico não pode ter duas consultas no mesmo horário de início")
        void conflito() throws SQLException {
            consulta("2026-10-01 09:00:00", 30, null, "100");
            assertThatThrownBy(() -> consulta("2026-10-01 09:00:00", 30, null, "100")).isInstanceOf(SQLException.class);
        }

        @Test
        @DisplayName("chaves estrangeiras: paciente precisa existir e não pode ser apagado se tiver consultas")
        void fk() throws SQLException {
            assertThatThrownBy(() -> banco.executar("INSERT INTO consulta (paciente_id, medico_id, inicio, duracao_min, valor) "
                    + "VALUES (9999, (SELECT id FROM medico), TIMESTAMP '2026-10-01 10:00:00', 30, 100)"))
                    .isInstanceOf(SQLException.class);
            consulta("2026-10-01 09:00:00", 30, null, "100");
            assertThatThrownBy(() -> banco.executar("DELETE FROM paciente WHERE cpf = '52998224725'")).isInstanceOf(SQLException.class);
        }
    }
}
