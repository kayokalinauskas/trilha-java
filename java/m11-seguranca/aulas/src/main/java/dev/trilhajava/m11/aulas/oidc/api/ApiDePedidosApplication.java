package dev.trilhajava.m11.aulas.oidc.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** A API de pedidos: um Resource Server que só aceita access tokens emitidos pelo Keycloak. */
@SpringBootApplication
public class ApiDePedidosApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ApiDePedidosApplication.class)
                .properties("spring.config.name=api-de-pedidos") // lê api-de-pedidos.yml
                .run(args);
    }
}
