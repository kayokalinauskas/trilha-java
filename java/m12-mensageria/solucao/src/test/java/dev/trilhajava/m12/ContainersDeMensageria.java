package dev.trilhajava.m12;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

/**
 * Os brokers (e o banco) de verdade, em containers. O @ServiceConnection configura host, porta e credenciais
 * no Spring Boot. Cada teste importa só o que usa.
 */
public final class ContainersDeMensageria {

    private ContainersDeMensageria() {
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class Rabbit {

        @Bean
        @ServiceConnection
        RabbitMQContainer rabbit() {
            return new RabbitMQContainer("rabbitmq:4-alpine");
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class Kafka {

        @Bean
        @ServiceConnection
        KafkaContainer kafka() {
            // Imagem JVM oficial: a "native" (GraalVM) sobe mais rápido, mas quebrou com segfault no CI. Em KRaft: sem ZooKeeper desde o Kafka 4.
            return new KafkaContainer("apache/kafka:4.2.1");
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class Postgres {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:18");
        }
    }
}
