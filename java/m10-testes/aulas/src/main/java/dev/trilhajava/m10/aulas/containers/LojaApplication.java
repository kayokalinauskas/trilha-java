package dev.trilhajava.m10.aulas.containers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Aula 10.05: o carrinho de compras de uma loja, gravado no PostgreSQL com JdbcClient e Flyway.
 * Os testes sobem o banco num container (Testcontainers); rode-os com o Docker ligado.
 */
@SpringBootApplication
@PropertySource("classpath:aulas-containers.properties")
public class LojaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LojaApplication.class, args);
    }
}
