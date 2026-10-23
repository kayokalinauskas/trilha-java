package dev.trilhajava.m03.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 03.05 — Cache LRU")
class CacheLruTest {

    @Test
    @DisplayName("guarda e recupera valores")
    void basico() {
        CacheLru<String, Integer> cache = new CacheLru<>(2);
        cache.colocar("a", 1);
        assertThat(cache.obter("a")).isEqualTo(1);
        assertThat(cache.obter("x")).isNull();
        assertThat(cache.contem("a")).isTrue();
        assertThat(cache.tamanho()).isEqualTo(1);
    }

    @Test
    @DisplayName("ao passar da capacidade, remove o usado há mais tempo")
    void despejo() {
        CacheLru<String, Integer> cache = new CacheLru<>(2);
        cache.colocar("a", 1);
        cache.colocar("b", 2);
        cache.obter("a"); // "a" vira o mais recente; "b" passa a ser o menos recente
        cache.colocar("c", 3);
        assertThat(cache.contem("b")).isFalse();
        assertThat(cache.chavesDoMaisRecenteAoMaisAntigo()).containsExactly("c", "a");
    }

    @Test
    @DisplayName("atualizar uma chave existente conta como uso e não aumenta o tamanho")
    void atualizar() {
        CacheLru<String, Integer> cache = new CacheLru<>(2);
        cache.colocar("a", 1);
        cache.colocar("b", 2);
        cache.colocar("a", 10);
        cache.colocar("c", 3);
        assertThat(cache.obter("a")).isEqualTo(10);
        assertThat(cache.contem("b")).isFalse();
        assertThat(cache.tamanho()).isEqualTo(2);
    }

    @Test
    @DisplayName("contem NÃO conta como uso")
    void contemNaoAtualiza() {
        CacheLru<String, Integer> cache = new CacheLru<>(2);
        cache.colocar("a", 1);
        cache.colocar("b", 2);
        cache.contem("a");
        cache.colocar("c", 3);
        assertThat(cache.contem("a")).isFalse();
    }

    @Test
    @DisplayName("remover libera espaço")
    void remover() {
        CacheLru<String, Integer> cache = new CacheLru<>(2);
        cache.colocar("a", 1);
        cache.colocar("b", 2);
        assertThat(cache.remover("a")).isEqualTo(1);
        assertThat(cache.remover("a")).isNull();
        cache.colocar("c", 3);
        assertThat(cache.chavesDoMaisRecenteAoMaisAntigo()).containsExactly("c", "b");
    }

    @Test
    @DisplayName("avisa quem foi despejado (listener)")
    void listener() {
        List<String> despejados = new ArrayList<>();
        CacheLru<String, Integer> cache = new CacheLru<>(1, (chave, valor) -> despejados.add(chave + "=" + valor));
        cache.colocar("a", 1);
        cache.colocar("b", 2);
        cache.colocar("c", 3);
        assertThat(despejados).containsExactly("a=1", "b=2");
    }

    @Test
    @DisplayName("capacidade e chave inválidas são recusadas")
    void validacoes() {
        assertThatThrownBy(() -> new CacheLru<String, String>(0)).isInstanceOf(IllegalArgumentException.class);
        CacheLru<String, String> cache = new CacheLru<>(1);
        assertThatThrownBy(() -> cache.colocar(null, "x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("todas as operações são O(1): 300 mil operações com 50 mil chaves em menos de 2 s")
    void desempenho() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            CacheLru<Integer, Integer> cache = new CacheLru<>(50_000);
            for (int i = 0; i < 100_000; i++) {
                cache.colocar(i, i);
                cache.obter(i - 25_000);
                cache.obter(i / 2);
            }
            assertThat(cache.tamanho()).isEqualTo(50_000);
        });
    }
}
