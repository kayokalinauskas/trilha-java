package dev.trilhajava.m03.cache;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Exercício 03.05 — Cache LRU (Least Recently Used).
 * Rode: mvn test -Dtest=CacheLruTest
 *
 * Requisito não funcional: obter, colocar e remover em O(1).
 * Para aprender de verdade, implemente com HashMap + sua própria lista duplamente encadeada.
 * (Existe um atalho na JDK — descubra qual e compare depois, na solução.)
 */
public class CacheLru<K, V> {

    public CacheLru(int capacidade) {
        this(capacidade, (k, v) -> { });
    }

    /** @param aoDespejar chamado com (chave, valor) sempre que um item sai por falta de espaço */
    public CacheLru(int capacidade, BiConsumer<K, V> aoDespejar) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Valor da chave (e marca como usada agora), ou null se não existir. */
    public V obter(K chave) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Insere ou atualiza (e marca como usada agora). Se passar da capacidade, despeja o menos recente. */
    public void colocar(K chave, V valor) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Remove e devolve o valor, ou null se não existir. */
    public V remover(K chave) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Consulta sem marcar como usada. */
    public boolean contem(K chave) {
        throw new UnsupportedOperationException("TODO");
    }

    public int tamanho() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<K> chavesDoMaisRecenteAoMaisAntigo() {
        throw new UnsupportedOperationException("TODO");
    }
}
