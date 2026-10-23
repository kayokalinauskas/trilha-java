package dev.trilhajava.m11.aulas.senhas;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Um relógio que só anda quando o teste manda: "15 minutos depois" sem esperar 15 minutos. */
class RelogioDeTeste extends Clock {

    private Instant agora;

    RelogioDeTeste(Instant inicio) {
        this.agora = inicio;
    }

    void avancar(Duration quanto) {
        agora = agora.plus(quanto);
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
    public Clock withZone(ZoneId zona) {
        throw new UnsupportedOperationException("O relógio de teste trabalha só em UTC");
    }
}
