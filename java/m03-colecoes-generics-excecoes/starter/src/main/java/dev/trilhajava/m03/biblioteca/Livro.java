package dev.trilhajava.m03.biblioteca;

public record Livro(String isbn, String titulo) {

    public Livro {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN é obrigatório");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        isbn = isbn.strip();
        titulo = titulo.strip();
    }
}
