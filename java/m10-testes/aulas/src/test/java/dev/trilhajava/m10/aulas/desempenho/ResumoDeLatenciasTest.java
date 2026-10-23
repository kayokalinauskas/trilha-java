package dev.trilhajava.m10.aulas.desempenho;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ResumoDeLatenciasTest {

    @Test
    void percentisPeloPostoMaisProximo() {
        long[] latencias = new long[100];
        for (int i = 0; i < 100; i++) {
            latencias[i] = i + 1; // 1, 2, ..., 100 ms
        }

        var resumo = ResumoDeLatencias.de(latencias);

        assertThat(resumo.p50Ms()).isEqualTo(50);
        assertThat(resumo.p95Ms()).isEqualTo(95);
        assertThat(resumo.p99Ms()).isEqualTo(99);
        assertThat(resumo.maximoMs()).isEqualTo(100);
        assertThat(resumo.mediaMs()).isCloseTo(50.5, within(0.001));
    }

    @Test
    void aMediaEscondeACauda() {
        // 97 requisições de 20 ms e 3 de 2 segundos: 3% dos clientes esperaram 100 vezes mais.
        long[] latencias = new long[100];
        Arrays.fill(latencias, 0, 97, 20);
        Arrays.fill(latencias, 97, 100, 2_000);

        var resumo = ResumoDeLatencias.de(latencias);

        assertThat(resumo.mediaMs()).isCloseTo(79.4, within(0.001));
        assertThat(resumo.p95Ms()).isEqualTo(20);
        assertThat(resumo.p99Ms()).isEqualTo(2_000);
    }

    @Test
    void naoAceitaListaVazia() {
        assertThatThrownBy(() -> ResumoDeLatencias.de(new long[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
