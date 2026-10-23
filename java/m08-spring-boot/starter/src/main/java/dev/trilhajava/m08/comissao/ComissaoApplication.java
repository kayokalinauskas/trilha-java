package dev.trilhajava.m08.comissao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * Exercício 08.01. Rode: mvn test -Dtest='CalculadoraDeComissaoTest,ComissaoConfiguradaTest'
 *
 * TODO: o Spring só registra ComissaoProperties como bean se você pedir.
 * Pesquise @ConfigurationPropertiesScan (ou @EnableConfigurationProperties).
 */
@SpringBootApplication
public class ComissaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComissaoApplication.class, args);
    }
}
