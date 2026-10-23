package dev.trilhajava.m14.saude;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Exercício 14.03 — health checks, probes de liveness/readiness e modo de manutenção.
 * A configuração deste exercício fica em {@code application-saude.properties} (perfil "saude").
 */
@SpringBootApplication
public class SaudeApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SaudeApplication.class).profiles("saude").run(args);
    }
}
