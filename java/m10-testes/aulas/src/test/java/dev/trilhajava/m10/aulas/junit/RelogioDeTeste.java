package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.SAO_PAULO;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Um relógio que começa parado e só anda quando o teste manda. */
final class RelogioDeTeste extends Clock {

    private Instant instante;

    RelogioDeTeste(LocalDateTime inicio) {
        this.instante = inicio.atZone(SAO_PAULO).toInstant();
    }

    void avancar(Duration quanto) {
        instante = instante.plus(quanto);
    }

    @Override
    public Instant instant() {
        return instante;
    }

    @Override
    public ZoneId getZone() {
        return SAO_PAULO;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return Clock.fixed(instante, zona);
    }
}
