package dev.trilhajava.m08.cambio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Exercício 08.04. Rode: mvn test -Dtest=ConversorDeMoedasTest */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CambioApplication {

    public static void main(String[] args) {
        SpringApplication.run(CambioApplication.class, args);
    }
}
