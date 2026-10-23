package dev.trilhajava.m08.comissao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan // registra os records anotados com @ConfigurationProperties deste pacote
public class ComissaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComissaoApplication.class, args);
    }
}
