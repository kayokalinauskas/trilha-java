package dev.trilhajava.m13.reservas;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Exercício 13.02 — reserva de salas em arquitetura hexagonal (portas e adaptadores).
 * Esta classe fica FORA das camadas: é quem monta a aplicação.
 */
@SpringBootApplication
public class ReservasApplication {

    @Bean
    Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservasApplication.class, args);
    }
}
