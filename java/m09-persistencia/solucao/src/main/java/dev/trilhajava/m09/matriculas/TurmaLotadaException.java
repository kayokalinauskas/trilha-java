package dev.trilhajava.m09.matriculas;

public class TurmaLotadaException extends RuntimeException {

    public TurmaLotadaException(String codigoTurma) {
        super("A turma " + codigoTurma + " não tem vagas");
    }
}
