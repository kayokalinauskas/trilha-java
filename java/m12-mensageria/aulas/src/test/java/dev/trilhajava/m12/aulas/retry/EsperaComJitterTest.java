package dev.trilhajava.m12.aulas.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class EsperaComJitterTest {

    @Test
    void dobraAteOTetoEOJitterFicaEntreZeroEOValorExponencial() {
        var espera = new EsperaComJitter(Duration.ofMillis(200), Duration.ofSeconds(1), RandomGenerator.of("L64X128MixRandom"));

        assertThat(espera.semJitter(1)).isEqualTo(Duration.ofMillis(200));
        assertThat(espera.semJitter(3)).isEqualTo(Duration.ofMillis(800));
        assertThat(espera.semJitter(4)).isEqualTo(Duration.ofSeconds(1)); // 1.600 ms, cortado no teto
        assertThat(espera.semJitter(60)).isEqualTo(Duration.ofSeconds(1)); // sem estouro de long

        for (int i = 0; i < 1_000; i++) {
            assertThat(espera.comJitterTotal(3)).isBetween(Duration.ZERO, Duration.ofMillis(800));
        }
    }
}
