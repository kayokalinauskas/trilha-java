package dev.trilhajava.m03.catalogo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/**
 * Solução de referência do exercício 03.01.
 *
 * A normalização do código acontece em UM lugar (o construtor). Por isso equals/hashCode
 * podem comparar o campo direto: dois objetos com "ab-123" e "AB-123 " já nascem com "AB-123".
 */
public final class Peca {

    private final String codigo;
    private final String nome;
    private final BigDecimal preco;
    private final LocalDate atualizadoEm;

    public Peca(String codigo, String nome, BigDecimal preco, LocalDate atualizadoEm) {
        this.codigo = normalizar(codigo);
        this.nome = Objects.requireNonNull(nome, "nome");
        this.preco = Objects.requireNonNull(preco, "preco");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "atualizadoEm");
    }

    static String normalizar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código de fabricante é obrigatório");
        }
        return codigo.strip().toUpperCase(Locale.ROOT);
    }

    public String codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }

    public BigDecimal preco() {
        return preco;
    }

    public LocalDate atualizadoEm() {
        return atualizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Peca outra && codigo.equals(outra.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode(); // mesmos campos do equals — nem mais, nem menos
    }

    @Override
    public String toString() {
        return "Peca[" + codigo + ", " + nome + ", " + preco + ", " + atualizadoEm + "]";
    }
}
