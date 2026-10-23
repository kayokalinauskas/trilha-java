package dev.trilhajava.m06;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 06.01 — Contador de acessos thread-safe")
class ContadorDeAcessosTest {

    @Test
    @DisplayName("conta corretamente em uma thread")
    void simples() {
        ContadorDeAcessos c = new ContadorDeAcessos();
        c.registrar("/pedidos");
        c.registrar("/pedidos");
        c.registrar("/login");
        assertThat(c.total("/pedidos")).isEqualTo(2);
        assertThat(c.total("/nada")).isZero();
        assertThat(c.totalGeral()).isEqualTo(3);
    }

    @RepeatedTest(value = 3, name = "rodada {currentRepetition}")
    @DisplayName("16 threads x 20 mil registros não perdem nenhuma contagem")
    void concorrente() throws Exception {
        ContadorDeAcessos c = new ContadorDeAcessos();
        String[] endpoints = {"/pedidos", "/login", "/produtos", "/carrinho"};
        int threads = 16;
        int porThread = 20_000;
        CountDownLatch largada = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            List<Future<?>> tarefas = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tarefas.add(pool.submit(() -> {
                    largada.await(); // todas começam juntas: maximiza a disputa
                    for (int i = 0; i < porThread; i++) {
                        c.registrar(endpoints[i % endpoints.length]);
                    }
                    return null;
                }));
            }
            largada.countDown();
            for (Future<?> f : tarefas) {
                f.get();
            }
        }
        assertThat(c.totalGeral()).isEqualTo((long) threads * porThread);
        for (String e : endpoints) {
            assertThat(c.total(e)).as(e).isEqualTo((long) threads * porThread / endpoints.length);
        }
    }

    @Test
    @DisplayName("top endpoints por acessos; empate em ordem alfabética")
    void top() {
        ContadorDeAcessos c = new ContadorDeAcessos();
        for (int i = 0; i < 3; i++) c.registrar("/b");
        for (int i = 0; i < 3; i++) c.registrar("/a");
        c.registrar("/c");
        assertThat(c.topEndpoints(2)).containsExactly("/a", "/b");
    }
}
