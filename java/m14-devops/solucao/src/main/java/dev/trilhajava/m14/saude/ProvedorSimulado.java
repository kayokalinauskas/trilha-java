package dev.trilhajava.m14.saude;

import java.time.Duration;
import org.springframework.stereotype.Component;

/** Implementação de desenvolvimento: sempre responde em 12 ms. */
@Component
class ProvedorSimulado implements ProvedorDePagamentos {

    @Override
    public Duration ping() {
        return Duration.ofMillis(12);
    }
}
