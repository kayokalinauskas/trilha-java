package dev.trilhajava.m11.aulas.oidc.faturamento;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** O serviço de faturamento: não tem usuário logado; fala com a API de pedidos em nome próprio. */
@SpringBootApplication
public class FaturamentoApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FaturamentoApplication.class)
                .properties("spring.config.name=faturamento") // lê faturamento.yml
                .run(args);
    }
}
