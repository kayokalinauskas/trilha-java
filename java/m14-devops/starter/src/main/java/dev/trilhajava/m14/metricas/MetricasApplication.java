package dev.trilhajava.m14.metricas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Exercício 14.02 — métricas de negócio com Micrometer, expostas para o Prometheus. */
@SpringBootApplication
public class MetricasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricasApplication.class, args);
    }
}
