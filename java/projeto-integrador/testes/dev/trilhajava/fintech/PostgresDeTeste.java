package dev.trilhajava.fintech;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Um PostgreSQL 18 de verdade num container (Testcontainers), com a conexão configurada pelo
 * @ServiceConnection. Importe nos testes que precisam do banco real: @Import(PostgresDeTeste.class).
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresDeTeste {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18");
    }
}
