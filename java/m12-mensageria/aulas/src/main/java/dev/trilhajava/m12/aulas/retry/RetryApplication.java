package dev.trilhajava.m12.aulas.retry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** A aplicação da aula 12.06: emissão de notas fiscais (retry bloqueante) e envio por e-mail (tópicos de retry). */
@SpringBootApplication
@PropertySource("classpath:aula-12-06-retry.properties")
public class RetryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetryApplication.class, args);
    }
}
