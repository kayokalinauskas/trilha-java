package dev.trilhajava.m10.fila;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Exercício 10.03 — a aplicação da fila. Não há spring.datasource.* em fila.properties: nos testes, a conexão
 * vem do container PostgreSQL declarado em ContainersDeTeste.
 */
@SpringBootApplication
@PropertySource("classpath:fila.properties")
public class FilaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilaApplication.class, args);
    }
}
