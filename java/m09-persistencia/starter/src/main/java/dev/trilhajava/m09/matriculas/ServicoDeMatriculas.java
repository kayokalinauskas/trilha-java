package dev.trilhajava.m09.matriculas;

import java.util.List;

/**
 * Exercício 09.01 — mapeie as tabelas de src/main/resources/db/matriculas/V1__cria_tabelas.sql
 * como entidades JPA (Aluno, Turma, Matricula), crie os repositórios e implemente este serviço.
 *
 * O Hibernate está com ddl-auto=validate: se o mapeamento não bater com o schema, a aplicação
 * nem sobe — e a mensagem de erro diz o que está diferente.
 */
public class ServicoDeMatriculas {

    /** Matricula (ou reativa uma matrícula cancelada) e devolve o id da matrícula. */
    public long matricular(long alunoId, long turmaId) {
        throw new UnsupportedOperationException("TODO");
    }

    public void cancelar(long alunoId, long turmaId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Códigos das turmas em que o aluno tem matrícula ATIVA, em ordem alfabética. */
    public List<String> turmasDoAluno(long alunoId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Alunos com matrícula ATIVA na turma, por nome — em UMA consulta SQL. */
    public List<AlunoMatriculado> listaDeChamada(String codigoTurma) {
        throw new UnsupportedOperationException("TODO");
    }

    public long vagasRestantes(String codigoTurma) {
        throw new UnsupportedOperationException("TODO");
    }
}
