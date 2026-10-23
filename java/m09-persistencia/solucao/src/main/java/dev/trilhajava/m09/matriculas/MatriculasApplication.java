package dev.trilhajava.m09.matriculas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** Exercício 09.01. Rode: mvn test -Dtest=MatriculasTest */
@SpringBootApplication
@PropertySource("classpath:matriculas.properties")
public class MatriculasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatriculasApplication.class, args);
    }
}
