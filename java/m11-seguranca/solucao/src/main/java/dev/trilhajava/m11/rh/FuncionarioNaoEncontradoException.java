package dev.trilhajava.m11.rh;

/** 404: o funcionário não existe OU quem pergunta não pode saber que ele existe. */
public class FuncionarioNaoEncontradoException extends RuntimeException {

    public FuncionarioNaoEncontradoException(long id) {
        super("Funcionário " + id + " não encontrado");
    }
}
