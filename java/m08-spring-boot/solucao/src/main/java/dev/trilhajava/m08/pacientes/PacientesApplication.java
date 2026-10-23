package dev.trilhajava.m08.pacientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Exercício 08.02. Rode: mvn test -Dtest=PacientesApiTest — ou suba a API com o main e teste com curl. */
@SpringBootApplication
public class PacientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PacientesApplication.class, args);
    }
}
