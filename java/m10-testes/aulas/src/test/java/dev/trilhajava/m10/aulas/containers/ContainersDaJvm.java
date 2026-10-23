package dev.trilhajava.m10.aulas.containers;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Containers "singleton": campos de interface são static final, então existe UM objeto por JVM.
 * Toda classe que usar @ImportTestcontainers(ContainersDaJvm.class) recebe este mesmo
 * container, mesmo que o Spring monte contextos diferentes para ela. O Ryuk o remove quando
 * a JVM dos testes termina.
 */
public interface ContainersDaJvm {

    @Container
    @ServiceConnection
    PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18");
}
