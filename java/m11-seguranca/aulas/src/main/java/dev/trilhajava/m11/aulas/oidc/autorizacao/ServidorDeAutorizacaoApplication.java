package dev.trilhajava.m11.aulas.oidc.autorizacao;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * O provedor de identidade da loja, com o Spring Authorization Server (parte do Spring
 * Security 7). Faz o papel que o Keycloak, o Entra ID ou o Auth0 fariam: autentica pessoas,
 * registra clientes e emite tokens. Os clientes ficam em servidor-de-autorizacao.yml.
 */
@SpringBootApplication
public class ServidorDeAutorizacaoApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServidorDeAutorizacaoApplication.class)
                .properties("spring.config.name=servidor-de-autorizacao")
                .run(args);
    }
}
