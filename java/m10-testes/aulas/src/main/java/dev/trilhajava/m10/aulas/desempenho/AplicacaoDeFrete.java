package dev.trilhajava.m10.aulas.desempenho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Aula 10.08: a cotação de frete que vamos colocar sob carga.
 * O perfil Maven "carga" sobe esta aplicação, roda o Gatling contra ela e a derruba no fim.
 */
@SpringBootApplication
@PropertySource("classpath:aulas-desempenho.properties")
public class AplicacaoDeFrete {

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoDeFrete.class, args);
    }
}
