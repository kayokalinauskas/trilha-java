package dev.trilhajava.m11.aulas.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Aula 11.03: a API de extrato de um banco digital. Ela não emite tokens: só os valida
 * (é um OAuth2 Resource Server). Quem emite é o servidor de autorização do banco.
 */
@SpringBootApplication
@PropertySource("classpath:aulas-jwt.properties")
public class AplicacaoDeExtrato {

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoDeExtrato.class, args);
    }
}
