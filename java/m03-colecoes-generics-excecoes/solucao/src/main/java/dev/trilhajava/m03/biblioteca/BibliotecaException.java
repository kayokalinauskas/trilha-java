package dev.trilhajava.m03.biblioteca;

/**
 * Raiz das exceções de negócio. Unchecked (RuntimeException) porque:
 * - quem chama geralmente não tem como "consertar" a situação ali mesmo;
 * - num backend, um handler global (ex.: @RestControllerAdvice, módulo 08) traduz o código em resposta HTTP;
 * - evitar "throws" em cascata por todas as camadas.
 * Ser abstrata impede lançar um erro genérico "BibliotecaException" sem significado.
 */
public abstract class BibliotecaException extends RuntimeException {

    private final String codigo;

    protected BibliotecaException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
