package dev.trilhajava.m13.loja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Exercício 13.03 — um monólito modular com Spring Modulith.
 * Cada pacote logo abaixo deste (pedidos, estoque, faturamento) é um módulo da aplicação.
 */
@SpringBootApplication
@EnableAsync
@PropertySource("classpath:loja.properties")
public class LojaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LojaApplication.class, args);
    }
}
