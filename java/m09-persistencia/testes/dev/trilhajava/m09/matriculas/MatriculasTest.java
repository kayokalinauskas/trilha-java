package dev.trilhajava.m09.matriculas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

@SpringBootTest
@DisplayName("Exercício 09.01 — Matrículas: entidades, relacionamentos e consultas")
class MatriculasTest {

    @Autowired
    ServicoDeMatriculas servico;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManagerFactory emf;

    Statistics estatisticas;

    @BeforeEach
    void preparar() {
        estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
    }

    /** Dados criados direto no banco (SQL), independentes das suas entidades. */
    long aluno(String nome) {
        return new SimpleJdbcInsert(jdbc).withTableName("aluno").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("nome", nome, "email", UUID.randomUUID() + "@escola.dev")).longValue();
    }

    String codigoNovo() {
        return "T-" + UUID.randomUUID().toString().substring(0, 8);
    }

    long turma(String codigo, int vagas) {
        return new SimpleJdbcInsert(jdbc).withTableName("turma").usingGeneratedKeyColumns("id")
                .executeAndReturnKey(Map.of("codigo", codigo, "disciplina", "Banco de Dados", "vagas", vagas)).longValue();
    }

    @Test
    @DisplayName("matricula e lista as turmas ativas do aluno em ordem de código")
    void matricula() {
        long ana = aluno("Ana");
        String b = "B-" + UUID.randomUUID().toString().substring(0, 6);
        String a = "A-" + UUID.randomUUID().toString().substring(0, 6);
        long turmaB = turma(b, 30);
        long turmaA = turma(a, 30);

        long id = servico.matricular(ana, turmaB);
        servico.matricular(ana, turmaA);

        assertThat(id).isPositive();
        assertThat(servico.turmasDoAluno(ana)).containsExactly(a, b);
        assertThat(jdbc.queryForObject("select status from matricula where id = ?", String.class, id)).isEqualTo("ATIVA");
    }

    @Test
    @DisplayName("lista de chamada: só matrículas ativas, por nome, em UMA consulta SQL")
    void listaDeChamada() {
        String codigo = codigoNovo();
        long turma = turma(codigo, 10);
        long carla = aluno("Carla");
        long ana = aluno("Ana");
        long bruno = aluno("Bruno");
        servico.matricular(carla, turma);
        servico.matricular(ana, turma);
        servico.matricular(bruno, turma);
        servico.cancelar(bruno, turma);

        estatisticas.clear();
        List<AlunoMatriculado> chamada = servico.listaDeChamada(codigo);

        assertThat(chamada).extracting(AlunoMatriculado::nome).containsExactly("Ana", "Carla");
        assertThat(chamada).allSatisfy(al -> {
            assertThat(al.email()).endsWith("@escola.dev");
            assertThat(al.matriculadaEm()).isNotNull();
        });
        assertThat(estatisticas.getPrepareStatementCount()).as("comandos SQL executados").isEqualTo(1);
    }

    @Test
    @DisplayName("matrícula ativa repetida → MatriculaDuplicadaException")
    void duplicada() {
        long aluno = aluno("Davi");
        long turma = turma(codigoNovo(), 5);
        servico.matricular(aluno, turma);
        assertThatThrownBy(() -> servico.matricular(aluno, turma)).isInstanceOf(MatriculaDuplicadaException.class);
    }

    @Test
    @DisplayName("turma lotada → TurmaLotadaException; cancelar libera a vaga")
    void lotada() {
        String codigo = codigoNovo();
        long turma = turma(codigo, 2);
        long a1 = aluno("Elis");
        long a2 = aluno("Fábio");
        long a3 = aluno("Gil");
        servico.matricular(a1, turma);
        servico.matricular(a2, turma);
        assertThat(servico.vagasRestantes(codigo)).isZero();

        assertThatThrownBy(() -> servico.matricular(a3, turma))
                .isInstanceOf(TurmaLotadaException.class)
                .hasMessageContaining(codigo);

        servico.cancelar(a1, turma);
        assertThat(servico.vagasRestantes(codigo)).isEqualTo(1);
        servico.matricular(a3, turma);
        assertThat(servico.vagasRestantes(codigo)).isZero();
    }

    @Test
    @DisplayName("rematrícula depois de cancelar reaproveita a mesma linha (constraint UNIQUE aluno+turma)")
    void rematricula() {
        long aluno = aluno("Helena");
        long turma = turma(codigoNovo(), 5);
        long primeira = servico.matricular(aluno, turma);
        servico.cancelar(aluno, turma);
        assertThat(servico.turmasDoAluno(aluno)).isEmpty();

        long segunda = servico.matricular(aluno, turma);

        assertThat(segunda).isEqualTo(primeira);
        assertThat(jdbc.queryForObject("select count(*) from matricula where aluno_id = ?", Integer.class, aluno)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select status from matricula where id = ?", String.class, segunda)).isEqualTo("ATIVA");
    }

    @Test
    @DisplayName("aluno, turma ou matrícula inexistente → RegistroNaoEncontradoException")
    void inexistentes() {
        long aluno = aluno("Iris");
        long turma = turma(codigoNovo(), 5);
        assertThatThrownBy(() -> servico.matricular(999_999, turma)).isInstanceOf(RegistroNaoEncontradoException.class);
        assertThatThrownBy(() -> servico.matricular(aluno, 999_999)).isInstanceOf(RegistroNaoEncontradoException.class);
        assertThatThrownBy(() -> servico.cancelar(aluno, turma)).isInstanceOf(RegistroNaoEncontradoException.class);
        assertThatThrownBy(() -> servico.vagasRestantes("NAO-EXISTE")).isInstanceOf(RegistroNaoEncontradoException.class);
    }

    @Test
    @DisplayName("12 alunos disputam 3 vagas ao mesmo tempo: exatamente 3 conseguem")
    void concorrencia() throws Exception {
        String codigo = codigoNovo();
        long turma = turma(codigo, 3);
        List<Long> candidatos = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            candidatos.add(aluno("Candidato " + i));
        }
        var largada = new CountDownLatch(1);
        var sucessos = new AtomicInteger();
        var lotadas = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(12)) {
            List<Future<?>> tarefas = new ArrayList<>();
            for (long candidato : candidatos) {
                tarefas.add(executor.submit(() -> {
                    largada.await();
                    try {
                        servico.matricular(candidato, turma);
                        sucessos.incrementAndGet();
                    } catch (TurmaLotadaException e) {
                        lotadas.incrementAndGet();
                    }
                    return null;
                }));
            }
            largada.countDown();
            for (Future<?> tarefa : tarefas) {
                tarefa.get(); // propaga qualquer outra exceção
            }
        }
        assertThat(sucessos).hasValue(3);
        assertThat(lotadas).hasValue(9);
        assertThat(jdbc.queryForObject("select count(*) from matricula where turma_id = ? and status = 'ATIVA'",
                Integer.class, turma)).isEqualTo(3);
    }
}
