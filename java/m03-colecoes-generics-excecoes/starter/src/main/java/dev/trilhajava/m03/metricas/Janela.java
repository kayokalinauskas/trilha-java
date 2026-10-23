package dev.trilhajava.m03.metricas;

import java.util.List;

/**
 * Exercício 03.04 — janela deslizante de tamanho fixo (buffer circular).
 * Quando cheia, adicionar descarta o elemento mais antigo. Todas as operações em O(1), exceto ultimos() (O(n)).
 *
 * Desafio: você não pode fazer "new T[capacidade]" em Java. Por quê? Como contornar?
 */
public final class Janela<T> {

    public Janela(int capacidade) {
        throw new UnsupportedOperationException("TODO");
    }

    public void adicionar(T elemento) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Cópia imutável, do mais antigo ao mais recente. */
    public List<T> ultimos() {
        throw new UnsupportedOperationException("TODO");
    }

    public int tamanho() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean estaCheia() {
        throw new UnsupportedOperationException("TODO");
    }
}
