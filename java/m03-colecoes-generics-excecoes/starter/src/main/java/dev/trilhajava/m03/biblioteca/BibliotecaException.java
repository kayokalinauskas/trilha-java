package dev.trilhajava.m03.biblioteca;

/**
 * Exercício 03.03 — raiz das exceções de NEGÓCIO da biblioteca.
 * Todas carregam um código estável (ex.: "LIVRO_NAO_ENCONTRADO"), útil para APIs e logs.
 *
 * TODO: decida: checked ou unchecked? Justifique no seu README do exercício.
 */
public abstract class BibliotecaException extends RuntimeException {

    protected BibliotecaException(String codigo, String mensagem) {
        super(mensagem);
        // TODO: guarde o código
    }

    public String codigo() {
        throw new UnsupportedOperationException("TODO");
    }
}
