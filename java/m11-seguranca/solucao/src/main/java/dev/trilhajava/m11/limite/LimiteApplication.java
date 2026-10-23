package dev.trilhajava.m11.limite;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Exercício 11.04 — um login de mentira, protegido pelo limite de requisições. Suba pelo ▶ do IntelliJ ou com
 * {@code mvn spring-boot:run "-Dspring-boot.run.main-class=dev.trilhajava.m11.limite.LimiteApplication"}.
 */
@SpringBootApplication
public class LimiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(LimiteApplication.class, args);
    }

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
