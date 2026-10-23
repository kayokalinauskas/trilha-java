package dev.trilhajava.m08.cambio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Um Clock que só anda quando o teste manda. */
final class RelogioDeTeste extends Clock {

    private volatile Instant agora = Instant.parse("2026-09-28T13:00:00Z");

    void avancar(Duration duracao) {
        agora = agora.plus(duracao);
    }

    @Override
    public Instant instant() {
        return agora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }
}
