package dev.trilhajava.m11.aulas.defesas;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Um {@link Clock} que só anda quando mandam: o jeito de testar "12 segundos depois" em zero segundos.
 * Usado nos testes e na simulação; em produção, o relógio é {@code Clock.systemUTC()}.
 */
public class RelogioAjustavel extends Clock {

    private volatile Instant agora;

    public RelogioAjustavel(Instant inicio) {
        this.agora = inicio;
    }

    public void avancar(Duration quanto) {
        agora = agora.plus(quanto);
    }

    /** Simula um ajuste de NTP para trás. */
    public void voltar(Duration quanto) {
        agora = agora.minus(quanto);
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
        return Clock.fixed(agora, zona);
    }
}
