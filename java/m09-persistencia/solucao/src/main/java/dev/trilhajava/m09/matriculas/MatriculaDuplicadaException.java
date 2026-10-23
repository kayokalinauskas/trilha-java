package dev.trilhajava.m09.matriculas;

public class MatriculaDuplicadaException extends RuntimeException {

    public MatriculaDuplicadaException(long alunoId, long turmaId) {
        super("O aluno " + alunoId + " já está matriculado na turma " + turmaId);
    }
}
