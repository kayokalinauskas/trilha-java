package dev.trilhajava.m03.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Solução de referência do exercício 03.05.
 *
 * HashMap<K, No> dá acesso O(1) ao nó; a lista duplamente encadeada dá O(1) para
 * "mover para a frente" e "remover do fim". Os nós sentinela (cabeca/cauda) eliminam
 * os ifs de "lista vazia" e "primeiro/último elemento".
 *
 *   cabeca <-> [mais recente] <-> ... <-> [menos recente] <-> cauda
 */
public class CacheLru<K, V> {

    private static final class No<K, V> {
        K chave;
        V valor;
        No<K, V> anterior;
        No<K, V> proximo;

        No(K chave, V valor) {
            this.chave = chave;
            this.valor = valor;
        }
    }

    private final int capacidade;
    private final BiConsumer<K, V> aoDespejar;
    private final Map<K, No<K, V>> indice;
    private final No<K, V> cabeca = new No<>(null, null);
    private final No<K, V> cauda = new No<>(null, null);

    public CacheLru(int capacidade) {
        this(capacidade, (k, v) -> { });
    }

    public CacheLru(int capacidade, BiConsumer<K, V> aoDespejar) {
        if (capacidade < 1) {
            throw new IllegalArgumentException("Capacidade deve ser pelo menos 1");
        }
        this.capacidade = capacidade;
        this.aoDespejar = aoDespejar == null ? (k, v) -> { } : aoDespejar;
        // Dimensionar o HashMap evita rehash durante o uso (0.75 é o fator de carga padrão).
        this.indice = new HashMap<>((int) (capacidade / 0.75f) + 1);
        cabeca.proximo = cauda;
        cauda.anterior = cabeca;
    }

    public V obter(K chave) {
        No<K, V> no = indice.get(chave);
        if (no == null) {
            return null;
        }
        moverParaFrente(no);
        return no.valor;
    }

    public void colocar(K chave, V valor) {
        if (chave == null) {
            throw new IllegalArgumentException("Chave não pode ser nula");
        }
        No<K, V> existente = indice.get(chave);
        if (existente != null) {
            existente.valor = valor;
            moverParaFrente(existente);
            return;
        }
        if (indice.size() == capacidade) {
            No<K, V> menosRecente = cauda.anterior;
            desligar(menosRecente);
            indice.remove(menosRecente.chave);
            aoDespejar.accept(menosRecente.chave, menosRecente.valor);
        }
        No<K, V> novo = new No<>(chave, valor);
        inserirNaFrente(novo);
        indice.put(chave, novo);
    }

    public V remover(K chave) {
        No<K, V> no = indice.remove(chave);
        if (no == null) {
            return null;
        }
        desligar(no);
        return no.valor;
    }

    public boolean contem(K chave) {
        return indice.containsKey(chave);
    }

    public int tamanho() {
        return indice.size();
    }

    public List<K> chavesDoMaisRecenteAoMaisAntigo() {
        List<K> chaves = new ArrayList<>(indice.size());
        for (No<K, V> no = cabeca.proximo; no != cauda; no = no.proximo) {
            chaves.add(no.chave);
        }
        return List.copyOf(chaves);
    }

    private void moverParaFrente(No<K, V> no) {
        desligar(no);
        inserirNaFrente(no);
    }

    private void inserirNaFrente(No<K, V> no) {
        no.anterior = cabeca;
        no.proximo = cabeca.proximo;
        cabeca.proximo.anterior = no;
        cabeca.proximo = no;
    }

    private void desligar(No<K, V> no) {
        no.anterior.proximo = no.proximo;
        no.proximo.anterior = no.anterior;
    }
}
