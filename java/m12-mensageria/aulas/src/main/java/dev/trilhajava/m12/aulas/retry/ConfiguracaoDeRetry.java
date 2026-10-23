package dev.trilhajava.m12.aulas.retry;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import tools.jackson.core.JacksonException;

@Configuration
class ConfiguracaoDeRetry {

    static final String PEDIDOS_PAGOS = "pedidos.pagos";
    static final String PEDIDOS_PAGOS_DLT = PEDIDOS_PAGOS + "-dlt"; // sufixo padrão do Spring Kafka 4
    static final String NOTAS_EMITIDAS = "notas.emitidas";

    @Bean
    NewTopic pedidosPagos() {
        return TopicBuilder.name(PEDIDOS_PAGOS).partitions(3).replicas(1).build();
    }

    /** O recuperador publica na partição de MESMO número: o DLT precisa de pelo menos 3 partições. */
    @Bean
    NewTopic pedidosPagosDlt() {
        return TopicBuilder.name(PEDIDOS_PAGOS_DLT).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic notasEmitidas() {
        return TopicBuilder.name(NOTAS_EMITIDAS).partitions(3).replicas(1).build();
    }

    /**
     * Retry BLOQUEANTE para os listeners comuns (o Spring Boot liga este bean na fábrica de listeners).
     * Esperas de ~200, ~400 e ~800 ms (com até 50 ms de jitter, multiplicado a cada tentativa);
     * 1 tentativa + 3 repetições; depois, DLT. Falhas permanentes vão direto, sem repetir.
     */
    @Bean
    DefaultErrorHandler tratadorDeErros(KafkaOperations<String, String> kafka, MonitorDeFalhas monitor) {
        var espera = new ExponentialBackOff(200, 2.0);
        espera.setJitter(50);
        espera.setMaxInterval(2_000);
        espera.setMaxAttempts(3);

        var paraDlt = new DeadLetterPublishingRecoverer(kafka);
        var tratador = new DefaultErrorHandler(paraDlt, espera);
        tratador.addNotRetryableExceptions(MensagemInvalidaException.class, JacksonException.class);
        tratador.setRetryListeners(monitor);
        return tratador;
    }
}
