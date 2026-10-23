package dev.trilhajava.m03.biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Exercício 03.03 — Biblioteca.
 * Rode: mvn test -Dtest=BibliotecaTest
 *
 * Ordem das verificações em emprestar: livro existe -> usuário sem atraso -> limite de 3 -> exemplar disponível.
 */
public class Biblioteca {

    public static final int PRAZO_DIAS = 14;
    public static final int LIMITE_ATIVOS = 3;

    public void cadastrar(Livro livro, int exemplares) {
        throw new UnsupportedOperationException("TODO");
    }

    public int disponiveis(String isbn) {
        throw new UnsupportedOperationException("TODO: lança LivroNaoEncontradoException se não existir");
    }

    public Emprestimo emprestar(String isbn, String usuarioId, LocalDate data) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Devolve a multa: R$ 2,00 por dia de atraso, com teto de R$ 50,00. */
    public BigDecimal devolver(long emprestimoId, LocalDate data) {
        throw new UnsupportedOperationException("TODO");
    }
}
