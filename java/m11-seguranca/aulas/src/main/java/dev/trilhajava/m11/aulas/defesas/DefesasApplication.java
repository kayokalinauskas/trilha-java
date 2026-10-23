package dev.trilhajava.m11.aulas.defesas;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/** A API de extratos da conta digital, com as defesas desta aula. */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PropriedadesDoJwt.class)
public class DefesasApplication {

    public static void main(String[] args) {
        SpringApplication.run(DefesasApplication.class, args);
    }

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    /** 60 créditos por minuto por cliente: 1 por segundo, com rajada de até 60. */
    @Bean
    LimitadorDeCreditos limitadorDeCreditos(Clock relogio) {
        return new LimitadorDeCreditos(new PoliticaDeLimite("por-cliente", 60, Duration.ofMinutes(1)), relogio);
    }

    @Bean
    JwtDecoder jwtDecoder(PropriedadesDoJwt propriedades) {
        var chave = new SecretKeySpec(propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** Baldes cheios não guardam informação: a cada minuto, saem da memória. */
    @Bean
    LimpezaDeCotas limpezaDeCotas(LimitadorDeCreditos limitador) {
        return new LimpezaDeCotas(limitador);
    }

    static class LimpezaDeCotas {
        private final LimitadorDeCreditos limitador;

        LimpezaDeCotas(LimitadorDeCreditos limitador) {
            this.limitador = limitador;
        }

        @Scheduled(fixedDelayString = "PT1M")
        void limpar() {
            limitador.removerOciosos();
        }
    }
}
