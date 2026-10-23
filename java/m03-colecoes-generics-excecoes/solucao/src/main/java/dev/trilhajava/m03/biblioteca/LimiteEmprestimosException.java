package dev.trilhajava.m03.biblioteca;

public class LimiteEmprestimosException extends BibliotecaException {

    public LimiteEmprestimosException(String usuarioId, int limite) {
        super("LIMITE_EMPRESTIMOS", "Usuário " + usuarioId + " já tem " + limite + " empréstimos ativos (limite: " + limite + ")");
    }
}
