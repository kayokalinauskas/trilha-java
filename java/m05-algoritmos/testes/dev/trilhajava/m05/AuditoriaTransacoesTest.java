package dev.trilhajava.m05;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 05.01 — Auditoria de transações (hashing)")
class AuditoriaTransacoesTest {

    @Test
    @DisplayName("par de transações que soma o valor contestado")
    void par() {
        assertThat(AuditoriaTransacoes.parQueSoma(new long[] {1500, 700, 300, 1200}, 1500)).containsExactly(2, 3);
        assertThat(AuditoriaTransacoes.parQueSoma(new long[] {500, 500}, 1000)).containsExactly(0, 1);
        assertThat(AuditoriaTransacoes.parQueSoma(new long[] {100, 200}, 1000)).isEmpty();
        assertThat(AuditoriaTransacoes.parQueSoma(new long[] {1000}, 1000)).as("não usa o mesmo item duas vezes").isEmpty();
    }

    @Test
    @DisplayName("duplicata dentro de uma janela de k posições")
    void duplicataEmJanela() {
        String[] ids = {"t1", "t2", "t3", "t1", "t4"};
        assertThat(AuditoriaTransacoes.duplicataEmJanela(ids, 3)).isTrue();
        assertThat(AuditoriaTransacoes.duplicataEmJanela(ids, 2)).isFalse();
        assertThat(AuditoriaTransacoes.duplicataEmJanela(new String[] {}, 5)).isFalse();
    }

    @Test
    @DisplayName("primeiro cliente que aparece uma única vez")
    void primeiroUnico() {
        assertThat(AuditoriaTransacoes.primeiroClienteUnico(new String[] {"ana", "bia", "ana", "caio", "bia"})).contains("caio");
        assertThat(AuditoriaTransacoes.primeiroClienteUnico(new String[] {"ana", "ana"})).isEmpty();
    }

    @Test
    @DisplayName("maior sequência de dias consecutivos com compra, em qualquer ordem")
    void consecutivos() {
        assertThat(AuditoriaTransacoes.maiorSequenciaDeDias(new int[] {100, 4, 200, 1, 3, 2})).isEqualTo(4);
        assertThat(AuditoriaTransacoes.maiorSequenciaDeDias(new int[] {5, 5, 6})).isEqualTo(2);
        assertThat(AuditoriaTransacoes.maiorSequenciaDeDias(new int[] {})).isZero();
    }

    @Test
    @DisplayName("tudo em O(n): 1 milhão de itens em menos de 2 s")
    void desempenho() {
        long[] valores = new Random(42).longs(1_000_000, 1, 1_000_000_000L).toArray();
        int[] dias = new Random(7).ints(1_000_000, 0, 5_000_000).toArray();
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            AuditoriaTransacoes.parQueSoma(valores, -1);
            AuditoriaTransacoes.maiorSequenciaDeDias(dias);
        });
    }
}
