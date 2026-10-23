package dev.trilhajava.fintech.infra.observabilidade;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Componente "outbox" do /actuator/health e gauge fintech.outbox.pendentes.
 *
 * Eventos pendentes são normais (o relay publica a cada meio segundo). O sintoma de problema é o evento
 * pendente mais ANTIGO: se ele está parado há mais de 10 minutos, o relay ou o broker estão com defeito
 * e as notificações não estão saindo. Isso fica visível para o admin e para os alertas, mas NÃO entra na
 * readiness: tirar a aplicação do balanceador não faria o Kafka voltar.
 */
@Component("outbox")
class SaudeDaOutbox implements HealthIndicator {

    private final JdbcClient jdbc;
    private final Clock clock;
    private final Duration atrasoMaximo;

    SaudeDaOutbox(JdbcClient jdbc, Clock clock, MeterRegistry metricas,
            @Value("${fintech.observabilidade.outbox-atraso-maximo:10m}") Duration atrasoMaximo) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.atrasoMaximo = atrasoMaximo;
        // A função roda a cada coleta (scrape) do Prometheus, não a cada evento.
        Gauge.builder("fintech.outbox.pendentes", this, SaudeDaOutbox::pendentes)
                .description("Eventos gravados na outbox e ainda não publicados")
                .register(metricas);
    }

    double pendentes() {
        return jdbc.sql("SELECT COUNT(*) FROM evento_saida WHERE publicado_em IS NULL").query(Long.class).single();
    }

    @Override
    public Health health() {
        long pendentes = (long) pendentes();
        // MIN de nenhuma linha é uma linha com NULL (e não zero linhas).
        Timestamp maisAntigo = jdbc.sql("SELECT MIN(criado_em) FROM evento_saida WHERE publicado_em IS NULL")
                .query(Timestamp.class).list().getFirst();
        if (maisAntigo == null) {
            return Health.up().withDetail("pendentes", 0L).build();
        }
        Duration atraso = Duration.between(maisAntigo.toInstant(), Instant.now(clock));
        Health.Builder saude = atraso.compareTo(atrasoMaximo) > 0 ? Health.down() : Health.up();
        return saude.withDetail("pendentes", pendentes)
                .withDetail("atrasoDoMaisAntigoSegundos", Math.max(0, atraso.toSeconds()))
                .build();
    }
}
