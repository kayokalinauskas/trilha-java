package dev.trilhajava.m14.saude;

import java.time.Duration;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Saúde do provedor de pagamentos. O nome do componente vem do bean:
 * {@code provedorHealthIndicator} vira {@code provedor} no JSON do /actuator/health.
 */
@Component
public class ProvedorHealthIndicator implements HealthIndicator {

    static final Duration LIMITE = Duration.ofSeconds(1);

    private final ProvedorDePagamentos provedor;

    public ProvedorHealthIndicator(ProvedorDePagamentos provedor) {
        this.provedor = provedor;
    }

    @Override
    public Health health() {
        try {
            Duration latencia = provedor.ping();
            Health.Builder saude = latencia.compareTo(LIMITE) > 0 ? Health.down() : Health.up();
            return saude.withDetail("latenciaMs", latencia.toMillis()).build();
        } catch (RuntimeException e) {
            // Só o tipo do erro: a mensagem pode ter URL interna, usuário, etc.
            return Health.down().withDetail("erro", e.getClass().getSimpleName()).build();
        }
    }
}
