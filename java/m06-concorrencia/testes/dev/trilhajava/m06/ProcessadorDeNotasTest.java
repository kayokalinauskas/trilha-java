package dev.trilhajava.m06;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 06.04 — Emissão de notas em lote com virtual threads")
class ProcessadorDeNotasTest {

    @Test
    @DisplayName("2.000 emissões de 50 ms com limite de 200 simultâneas: rápido e sem passar do limite")
    void limite() {
        AtomicInteger simultaneas = new AtomicInteger();
        AtomicInteger picoObservado = new AtomicInteger();
        Function<String, String> emitir = id -> {
            int agora = simultaneas.incrementAndGet();
            picoObservado.accumulateAndGet(agora, Math::max);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                simultaneas.decrementAndGet();
            }
            return "NF-" + id;
        };
        List<String> ids = IntStream.range(0, 2_000).mapToObj(i -> "P" + i).toList();

        long inicio = System.nanoTime();
        ResultadoLote r = new ProcessadorDeNotas().processar(ids, emitir, 200);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        assertThat(r.sucessos()).hasSize(2_000);
        assertThat(r.sucessos().keySet()).containsExactlyElementsOf(ids);
        assertThat(picoObservado.get()).isLessThanOrEqualTo(200).isGreaterThan(50);
        assertThat(ms).as("sequencial levaria 100 s").isLessThan(5_000);
    }

    @Test
    @DisplayName("falhas são coletadas sem interromper o lote")
    void falhas() {
        Function<String, String> emitir = id -> {
            if (id.endsWith("3")) {
                throw new IllegalStateException("SEFAZ indisponível para " + id);
            }
            return "NF-" + id;
        };
        ResultadoLote r = new ProcessadorDeNotas().processar(List.of("P1", "P2", "P3", "P13", "P4"), emitir, 2);
        assertThat(r.sucessos().keySet()).containsExactly("P1", "P2", "P4");
        assertThat(r.falhas()).containsEntry("P3", "SEFAZ indisponível para P3").containsKey("P13");
    }

    @Test
    @DisplayName("limite de concorrência inválido é recusado")
    void invalido() {
        assertThatThrownBy(() -> new ProcessadorDeNotas().processar(List.of("x"), id -> id, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
