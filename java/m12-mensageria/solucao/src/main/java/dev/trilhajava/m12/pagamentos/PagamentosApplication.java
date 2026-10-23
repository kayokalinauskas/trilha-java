package dev.trilhajava.m12.pagamentos;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/** Exercício 12.04 — consumidor idempotente de eventos de pagamento. */
@SpringBootApplication
@PropertySource("classpath:pagamentos.properties")
public class PagamentosApplication {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    public static void main(String[] args) {
        SpringApplication.run(PagamentosApplication.class, args);
    }
}
