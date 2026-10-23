package dev.trilhajava.m03.biblioteca;

public class LivroIndisponivelException extends BibliotecaException {

    public LivroIndisponivelException(String isbn) {
        super("LIVRO_INDISPONIVEL", "TODO: escreva uma mensagem que ajude quem lê o log");
    }
}
