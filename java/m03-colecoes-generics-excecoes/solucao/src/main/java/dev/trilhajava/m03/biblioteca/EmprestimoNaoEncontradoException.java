package dev.trilhajava.m03.biblioteca;

public class EmprestimoNaoEncontradoException extends BibliotecaException {

    public EmprestimoNaoEncontradoException(long emprestimoId) {
        super("EMPRESTIMO_NAO_ENCONTRADO", "Empréstimo ativo não encontrado: " + emprestimoId);
    }
}
