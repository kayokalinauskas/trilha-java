package dev.trilhajava.m10.aulas.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Um PostgreSQL 18 de verdade como bean do Spring. O Spring inicia o container antes do
 * DataSource e o para quando o contexto fecha. @ServiceConnection lê do container a URL,
 * o usuário e a senha: nenhuma spring.datasource.* escrita à mão.
 *
 * <p>Use com @Import(PostgresDeTeste.class). Todas as classes que importam do mesmo jeito
 * compartilham o contexto (cache do Spring) e, portanto, o MESMO container.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresDeTeste {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18"); // a MESMA versão da produção, nunca "latest"
    }
}
