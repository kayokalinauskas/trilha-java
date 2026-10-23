package dev.trilhajava.m12.estoque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** Exercício 12.02 — projeção de estoque com Kafka: chaves, ordem, retry e DLT. */
@SpringBootApplication
@PropertySource("classpath:estoque.properties")
public class EstoqueApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstoqueApplication.class, args);
    }
}
