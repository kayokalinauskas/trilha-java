package dev.trilhajava.m11.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * Exercício 11.02 — a API do app de delivery. Suba pelo ▶ do IntelliJ ou com
 * {@code mvn spring-boot:run "-Dspring-boot.run.main-class=dev.trilhajava.m11.delivery.DeliveryApplication"}.
 */
@SpringBootApplication
@PropertySource("classpath:delivery.properties")
@EnableConfigurationProperties(JwtProperties.class)
public class DeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryApplication.class, args);
    }
}
