package dev.trilhajava.m01;

/**
 * Exercício 01.01 — Validador de CPF.
 * Rode: mvn test -Dtest=ValidadorCpfTest
 */
public final class ValidadorCpf {

    private ValidadorCpf() {
    }

    /** Remove tudo que não for dígito. {@code null} vira "". */
    public static String somenteDigitos(String texto) {
        throw new UnsupportedOperationException("TODO: implemente somenteDigitos");
    }

    /** Aceita CPF com ou sem máscara. Retorna false para qualquer entrada inválida (nunca lança exceção). */
    public static boolean ehValido(String cpf) {
        throw new UnsupportedOperationException("TODO: implemente ehValido");
    }

    /** Devolve o CPF no formato 000.000.000-00. Lança IllegalArgumentException("CPF inválido: ...") se inválido. */
    public static String formatar(String cpf) {
        throw new UnsupportedOperationException("TODO: implemente formatar");
    }
}
