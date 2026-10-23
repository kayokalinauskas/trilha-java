package dev.trilhajava.m11.aulas.oidc.portal;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** O portal da loja: aplicação web renderizada no servidor, com login pelo Keycloak (OpenID Connect). */
@SpringBootApplication
public class PortalApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PortalApplication.class)
                .properties("spring.config.name=portal") // lê portal.yml
                .run(args);
    }
}
