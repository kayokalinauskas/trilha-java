package dev.trilhajava.m13.aulas.hexagonal;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** A aplicação do programa de fidelidade (aula 13.03). O banco é um H2 em memória. */
@SpringBootApplication
public class FidelidadeApplication {

    public static void main(String[] args) {
        SpringApplication aplicacao = new SpringApplication(FidelidadeApplication.class);
        // Scripts numa pasta própria: o módulo das aulas é compartilhado por várias aplicações.
        aplicacao.setDefaultProperties(Map.of(
                "spring.sql.init.schema-locations", "classpath:hexagonal/schema.sql",
                "spring.sql.init.data-locations", "classpath:hexagonal/data.sql"));
        aplicacao.run(args);
    }
}
