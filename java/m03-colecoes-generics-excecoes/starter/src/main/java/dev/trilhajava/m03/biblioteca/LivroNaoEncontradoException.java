package dev.trilhajava.m03.biblioteca;

public class LivroNaoEncontradoException extends BibliotecaException {

    public LivroNaoEncontradoException(String isbn) {
        super("LIVRO_NAO_ENCONTRADO", "TODO: escreva uma mensagem que ajude quem lê o log");
    }
}
