package dev.trilhajava.m03.catalogo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Exercício 03.01 — a peça decide quando é "igual" a outra.
 * Regra: duas peças são iguais se o código de fabricante normalizado (strip + maiúsculas) for igual.
 * Nome, preço e data NÃO participam da igualdade.
 */
public final class Peca {

    public Peca(String codigo, String nome, BigDecimal preco, LocalDate atualizadoEm) {
        throw new UnsupportedOperationException("TODO: valide e normalize o código");
    }

    public String codigo() {
        throw new UnsupportedOperationException("TODO");
    }

    public String nome() {
        throw new UnsupportedOperationException("TODO");
    }

    public BigDecimal preco() {
        throw new UnsupportedOperationException("TODO");
    }

    public LocalDate atualizadoEm() {
        throw new UnsupportedOperationException("TODO");
    }

    // TODO: equals e hashCode (sempre os dois juntos!) e um toString útil para depuração.
}
