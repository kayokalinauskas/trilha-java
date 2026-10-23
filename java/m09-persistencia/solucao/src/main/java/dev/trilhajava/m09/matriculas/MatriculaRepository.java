package dev.trilhajava.m09.matriculas;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    Optional<Matricula> findByAlunoIdAndTurmaId(long alunoId, long turmaId);

    long countByTurmaIdAndStatus(long turmaId, StatusMatricula status);

    @Query("""
            select t.codigo from Matricula m join m.turma t
             where m.aluno.id = :alunoId and m.status = StatusMatricula.ATIVA
             order by t.codigo""")
    List<String> codigosDasTurmasAtivas(long alunoId);

    /** Projeção direto para o record: UMA consulta, sem carregar entidades. */
    @Query("""
            select new dev.trilhajava.m09.matriculas.AlunoMatriculado(a.nome, a.email, m.matriculadaEm)
              from Matricula m join m.aluno a join m.turma t
             where t.codigo = :codigoTurma and m.status = StatusMatricula.ATIVA
             order by a.nome""")
    List<AlunoMatriculado> listaDeChamada(String codigoTurma);
}
