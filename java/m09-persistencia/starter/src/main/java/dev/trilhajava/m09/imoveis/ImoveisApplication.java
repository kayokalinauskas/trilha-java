package dev.trilhajava.m09.imoveis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** Exercício 09.04. Rode: mvn test -Dtest=BuscaDeImoveisTest */
@SpringBootApplication
@PropertySource("classpath:imoveis.properties")
public class ImoveisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImoveisApplication.class, args);
    }
}
