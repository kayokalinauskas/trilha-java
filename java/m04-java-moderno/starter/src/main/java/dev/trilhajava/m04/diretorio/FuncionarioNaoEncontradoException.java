package dev.trilhajava.m04.diretorio;

public class FuncionarioNaoEncontradoException extends RuntimeException {

    public FuncionarioNaoEncontradoException(String matricula) {
        super("Funcionário não encontrado: " + matricula);
    }
}
