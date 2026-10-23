package dev.trilhajava.m09.transferencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** Exercício 09.03. Rode: mvn test -Dtest=TransferenciasTest */
@SpringBootApplication
@PropertySource("classpath:transferencias.properties")
public class TransferenciasApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferenciasApplication.class, args);
    }
}
