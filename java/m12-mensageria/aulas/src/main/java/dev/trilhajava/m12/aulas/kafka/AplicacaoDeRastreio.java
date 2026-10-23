package dev.trilhajava.m12.aulas.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** Aula 12.03: o rastreio de pedidos alimentado por eventos num tópico Kafka. */
@SpringBootApplication
@PropertySource("classpath:aula-kafka.properties")
public class AplicacaoDeRastreio {

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoDeRastreio.class, args);
    }
}
