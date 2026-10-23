package dev.trilhajava.m06;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 06.02 — Reserva de assentos sem overbooking")
class SalaDeCinemaTest {

    @Test
    @DisplayName("reserva simples e consulta")
    void simples() {
        SalaDeCinema sala = new SalaDeCinema(10);
        assertThat(sala.reservar(3, "ana")).isTrue();
        assertThat(sala.reservar(3, "bia")).isFalse();
        assertThat(sala.ocupante(3)).contains("ana");
        assertThat(sala.ocupante(4)).isEmpty();
        assertThat(sala.livres()).isEqualTo(9);
        assertThatThrownBy(() -> sala.reservar(10, "x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("reserva de vários assentos é tudo ou nada")
    void varios() {
        SalaDeCinema sala = new SalaDeCinema(10);
        sala.reservar(5, "ana");
        assertThat(sala.reservarVarios(Set.of(4, 5, 6), "bia")).isFalse();
        assertThat(sala.ocupante(4)).isEmpty();
        assertThat(sala.reservarVarios(Set.of(7, 8), "bia")).isTrue();
        assertThat(sala.livres()).isEqualTo(7);
    }

    @RepeatedTest(value = 5, name = "rodada {currentRepetition}")
    @DisplayName("200 pessoas disputando o mesmo assento: exatamente uma consegue")
    void disputaUmAssento() throws Exception {
        SalaDeCinema sala = new SalaDeCinema(1);
        List<Boolean> resultados = disputar(200, i -> () -> sala.reservar(0, "cliente-" + i));
        assertThat(resultados.stream().filter(b -> b).count()).isEqualTo(1);
    }

    @RepeatedTest(value = 5, name = "rodada {currentRepetition}")
    @DisplayName("grupos disputando pares sobrepostos nunca geram assento duplicado nem reserva pela metade")
    void disputaPares() throws Exception {
        SalaDeCinema sala = new SalaDeCinema(20);
        // grupo i quer os assentos {i, i+1}: vizinhos sempre disputam um assento em comum
        List<Boolean> resultados = disputar(19, i -> () -> sala.reservarVarios(Set.of(i, i + 1), "grupo-" + i));
        Map<String, Integer> assentosPorGrupo = new HashMap<>();
        for (int a = 0; a < 20; a++) {
            sala.ocupante(a).ifPresent(g -> assentosPorGrupo.merge(g, 1, Integer::sum));
        }
        long vencedores = resultados.stream().filter(b -> b).count();
        assertThat(assentosPorGrupo).hasSize((int) vencedores);
        assertThat(assentosPorGrupo.values()).allMatch(n -> n == 2);
        assertThat(sala.livres()).isEqualTo(20 - 2 * (int) vencedores);
    }

    private static List<Boolean> disputar(int n, java.util.function.IntFunction<Callable<Boolean>> tarefa) throws Exception {
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> futuros = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(32)) {
            for (int i = 0; i < n; i++) {
                Callable<Boolean> c = tarefa.apply(i);
                futuros.add(pool.submit(() -> {
                    largada.await();
                    return c.call();
                }));
            }
            largada.countDown();
            List<Boolean> r = new ArrayList<>();
            for (Future<Boolean> f : futuros) {
                r.add(f.get());
            }
            return r;
        }
    }
}
