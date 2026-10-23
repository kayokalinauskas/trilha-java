package dev.trilhajava.fintech;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Um Kafka 4 (KRaft) de verdade num container, com a conexão configurada pelo @ServiceConnection.
 * Importe nos testes que precisam do broker: @Import(KafkaDeTeste.class).
 */
@TestConfiguration(proxyBeanMethods = false)
public class KafkaDeTeste {

    @Bean
    @ServiceConnection
    KafkaContainer kafka() {
        return new KafkaContainer("apache/kafka:4.2.1");
    }
}
