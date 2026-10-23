package dev.trilhajava.m09.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/** Exercício 09.02. Rode: mvn test -Dtest=PainelDePedidosTest */
@SpringBootApplication
@PropertySource("classpath:pedidos.properties")
public class PedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedidosApplication.class, args);
    }
}
