package dev.trilhajava.m03.biblioteca;

public class LimiteEmprestimosException extends BibliotecaException {

    public LimiteEmprestimosException(String usuarioId, int limite) {
        super("LIMITE_EMPRESTIMOS", "TODO: escreva uma mensagem que ajude quem lê o log");
    }
}
