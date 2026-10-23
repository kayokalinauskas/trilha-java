package dev.trilhajava.m12.aulas.outbox;

import java.time.Duration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Liga o relay ao relógio do Spring. Numa aplicação Spring Boot: registre como bean e ponha
 * {@code @EnableScheduling} numa classe de configuração.
 */
public class AgendadorDoRelay {

    private final RelayDoOutbox relay;

    public AgendadorDoRelay(RelayDoOutbox relay) {
        this.relay = relay;
    }

    // fixedDelay: a próxima rodada começa X ms depois que a anterior TERMINOU (nunca se sobrepõem).
    @Scheduled(fixedDelayString = "${loja.outbox.relay.intervalo-ms:500}")
    public void publicarPendentes() {
        relay.executar();
    }

    // Uma vez por dia, às 3h: apaga o que foi publicado há mais de 7 dias.
    @Scheduled(cron = "${loja.outbox.limpeza.cron:0 0 3 * * *}")
    public void limpar() {
        relay.limpar(Duration.ofDays(7));
    }
}
