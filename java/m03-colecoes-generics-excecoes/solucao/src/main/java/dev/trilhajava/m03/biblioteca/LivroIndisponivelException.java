package dev.trilhajava.m03.biblioteca;

public class LivroIndisponivelException extends BibliotecaException {

    public LivroIndisponivelException(String isbn) {
        super("LIVRO_INDISPONIVEL", "Nenhum exemplar disponível para o ISBN " + isbn);
    }
}
