package dev.trilhajava.fintech.infra.eventos;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

/**
 * Tudo o que fala com o broker só existe com fintech.eventos.habilitados=true (o padrão). Com false —
 * como nos testes dos marcos 1 a 4, que rodam sem Kafka —, os eventos continuam sendo gravados na
 * outbox, mas ninguém os publica nem consome.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "fintech.eventos.habilitados", havingValue = "true", matchIfMissing = true)
class ConfiguracaoDeEventos {

    @Bean
    NewTopic transferencias() {
        return TopicBuilder.name(Eventos.TOPICO).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic transferenciasMortas() {
        return TopicBuilder.name(Eventos.TOPICO_DLT).partitions(3).replicas(1).build();
    }

    /** Falha transitória: 3 repetições com 1 s. Mensagem ilegível: direto para o DLT. */
    @Bean
    DefaultErrorHandler errosDoConsumidor(KafkaOperations<String, String> kafka) {
        var tratador = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafka), new FixedBackOff(1_000, 3));
        tratador.addNotRetryableExceptions(JacksonException.class, IllegalArgumentException.class,
                NullPointerException.class);
        return tratador;
    }
}
