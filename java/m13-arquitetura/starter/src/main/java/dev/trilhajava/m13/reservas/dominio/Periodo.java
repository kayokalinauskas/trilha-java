package dev.trilhajava.m13.reservas.dominio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** Um intervalo [inicio, fim): o fim é exclusivo, então 9h–10h e 10h–11h não se sobrepõem. */
public record Periodo(LocalDateTime inicio, LocalDateTime fim) {

    public Periodo {
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
        if (!fim.isAfter(inicio)) {
            throw new ReservaInvalidaException("o fim precisa ser depois do início");
        }
    }

    public Duration duracao() {
        return Duration.between(inicio, fim);
    }

    // Implemente aqui o exercício: public boolean sobrepoe(Periodo outro)
}
