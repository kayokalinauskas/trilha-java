package dev.trilhajava.m09.matriculas;

public class RegistroNaoEncontradoException extends RuntimeException {

    public RegistroNaoEncontradoException(String descricao) {
        super(descricao + " não encontrado(a)");
    }
}
