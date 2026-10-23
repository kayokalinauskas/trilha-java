package dev.trilhajava.m13.aulas.microsservicos.estoque;

import java.time.ZonedDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.accept.StandardApiVersionDeprecationHandler;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * O serviço de estoque: uma aplicação Spring Boot própria, com o seu processo, a sua porta
 * e os seus dados. Na aula ele sobe dentro do teste, numa porta aleatória.
 */
@SpringBootApplication
public class EstoqueAplicacao implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        StandardApiVersionDeprecationHandler descontinuacao = new StandardApiVersionDeprecationHandler();
        descontinuacao.configureVersion("1")
                .setDeprecationDate(ZonedDateTime.parse("2026-10-01T00:00:00Z"))
                .setSunsetDate(ZonedDateTime.parse("2027-04-01T00:00:00Z"));

        configurer.useRequestHeader("API-Version")   // a versão vem no cabeçalho
                .setDefaultVersion("1")              // clientes antigos não mandam nada
                .setDeprecationHandler(descontinuacao);
    }

    public static void main(String[] args) {
        SpringApplication.run(EstoqueAplicacao.class, args);
    }
}
