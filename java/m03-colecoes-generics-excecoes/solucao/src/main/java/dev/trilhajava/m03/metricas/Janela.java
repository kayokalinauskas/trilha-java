package dev.trilhajava.m03.metricas;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffer circular genérico.
 *
 * "new T[n]" não compila por causa do type erasure: em tempo de execução T não existe,
 * e arrays precisam saber o tipo exato dos elementos. A saída clássica (usada pelo próprio ArrayList)
 * é guardar em Object[] e fazer o cast na leitura — seguro aqui porque só T entra no array.
 */
public final class Janela<T> {

    private final Object[] elementos;
    private int inicio; // índice do mais antigo
    private int tamanho;

    public Janela(int capacidade) {
        if (capacidade < 1) {
            throw new IllegalArgumentException("Capacidade deve ser pelo menos 1");
        }
        this.elementos = new Object[capacidade];
    }

    public void adicionar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("Elemento não pode ser nulo");
        }
        int fim = (inicio + tamanho) % elementos.length;
        elementos[fim] = elemento;
        if (estaCheia()) {
            inicio = (inicio + 1) % elementos.length; // sobrescreveu o mais antigo
        } else {
            tamanho++;
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> ultimos() {
        List<T> resultado = new ArrayList<>(tamanho);
        for (int i = 0; i < tamanho; i++) {
            resultado.add((T) elementos[(inicio + i) % elementos.length]);
        }
        return List.copyOf(resultado);
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean estaCheia() {
        return tamanho == elementos.length;
    }
}
