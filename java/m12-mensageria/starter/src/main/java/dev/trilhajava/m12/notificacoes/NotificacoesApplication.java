package dev.trilhajava.m12.notificacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** Exercício 12.01 — roteamento de notificações com RabbitMQ. */
@SpringBootApplication
@PropertySource("classpath:notificacoes.properties")
public class NotificacoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacoesApplication.class, args);
    }
}
