package dev.trilhajava.m10.fila;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * O container vira um bean: o Spring o inicia antes do DataSource e o para no fim.
 * @ServiceConnection lê host, porta, usuário e senha do container e configura a conexão —
 * sem nenhuma spring.datasource.* escrita à mão.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContainersDeTeste {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18"); // a MESMA versão da produção
    }
}
