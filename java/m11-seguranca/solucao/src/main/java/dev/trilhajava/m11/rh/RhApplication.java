package dev.trilhajava.m11.rh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Exercício 11.03 — a API de RH. Suba pelo ▶ do IntelliJ ou com
 * {@code mvn spring-boot:run "-Dspring-boot.run.main-class=dev.trilhajava.m11.rh.RhApplication"}.
 */
@SpringBootApplication
@PropertySource("classpath:rh.properties")
public class RhApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhApplication.class, args);
    }
}
