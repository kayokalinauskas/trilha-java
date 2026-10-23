package dev.trilhajava.m12.estoque;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Configuration
class ConfiguracaoKafka {

    static final String TOPICO = "estoque.movimentos";
    static final String TOPICO_DLT = TOPICO + "-dlt"; // o sufixo padrão do Spring Kafka 4 (era ".DLT" até o 3.x)

    /** Criado pelo KafkaAdmin na subida. 3 partições = até 3 consumidores em paralelo no grupo. */
    @Bean
    NewTopic movimentos() {
        return TopicBuilder.name(TOPICO).partitions(3).replicas(1).build();
    }

    /** O recuperador publica na partição de mesmo número do DLT: ele precisa de pelo menos 3 partições. */
    @Bean
    NewTopic movimentosMortos() {
        return TopicBuilder.name(TOPICO_DLT).partitions(3).replicas(1).build();
    }

    /**
     * O Spring Boot liga este CommonErrorHandler na fábrica de listeners.
     * Falha transitória: 1 tentativa + 2 repetições com 200 ms entre elas; depois, DLT.
     * Falha permanente (JSON inválido, saldo negativo): direto para o DLT, sem repetir.
     */
    @Bean
    DefaultErrorHandler tratadorDeErros(KafkaOperations<String, String> kafka) {
        var paraDlt = new DeadLetterPublishingRecoverer(kafka);
        var tratador = new DefaultErrorHandler(paraDlt, new FixedBackOff(200, 2));
        tratador.addNotRetryableExceptions(
                SaldoNegativoException.class, JacksonException.class, IllegalArgumentException.class);
        return tratador;
    }
}
