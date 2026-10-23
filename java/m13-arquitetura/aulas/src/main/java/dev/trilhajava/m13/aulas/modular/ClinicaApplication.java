package dev.trilhajava.m13.aulas.modular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Aula 13.04 — uma clínica como monólito modular.
 * Cada pacote logo abaixo deste (agenda, cobranca, notificacoes) é um módulo da aplicação.
 */
@SpringBootApplication
@EnableAsync
@PropertySource("classpath:clinica-modular.properties")
public class ClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicaApplication.class, args);
    }
}
