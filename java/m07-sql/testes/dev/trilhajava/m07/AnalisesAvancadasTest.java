package dev.trilhajava.m07;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 07.03 — Análises com window functions e CTEs")
class AnalisesAvancadasTest {

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
    @DisplayName("q1: melhor cliente de cada cidade (RANK por cidade)")
    void q1() throws SQLException {
        assertThat(banco.nomesDasColunas("sql/0703/q1-melhor-cliente-por-cidade.sql")).containsExactly("cidade", "nome", "total");
        assertThat(banco.consultarArquivo("sql/0703/q1-melhor-cliente-por-cidade.sql")).containsExactly(
                List.of("Curitiba", "Elis", "360"), List.of("Recife", "Carla", "310"), List.of("São Paulo", "Bruno", "360"));
    }

    @Test
    @DisplayName("q2: faturamento por dia e acumulado (SUM OVER)")
    void q2() throws SQLException {
        assertThat(banco.consultarArquivo("sql/0703/q2-acumulado-diario.sql")).containsExactly(
                List.of("2026-01-05", "55", "55"),
                List.of("2026-01-07", "360", "415"),
                List.of("2026-01-20", "40", "455"),
                List.of("2026-02-02", "90", "545"),
                List.of("2026-02-15", "205", "750"),
                List.of("2026-03-01", "80", "830"),
                List.of("2026-03-03", "25", "855"),
                List.of("2026-03-05", "360", "1215"));
    }

    @Test
    @DisplayName("q3: faturamento mensal e variação em relação ao mês anterior (LAG)")
    void q3() throws SQLException {
        assertThat(banco.consultarArquivo("sql/0703/q3-variacao-mensal.sql")).containsExactly(
                Arrays.asList("2026-01", "455", null), // List.of não aceita null
                List.of("2026-02", "295", "-160"),
                List.of("2026-03", "465", "170"));
    }

    @Test
    @DisplayName("q4: dias sem nenhuma venda paga entre 01/03 e 07/03 (CTE recursiva)")
    void q4() throws SQLException {
        assertThat(banco.consultarArquivo("sql/0703/q4-dias-sem-venda.sql")).containsExactly(
                List.of("2026-03-02"), List.of("2026-03-04"), List.of("2026-03-06"), List.of("2026-03-07"));
    }
}
