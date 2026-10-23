package dev.trilhajava.m10.aulas.contrato.frete;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * O serviço de frete da aula 10.07: o PROVEDOR do contrato. Os testes de fatia e os testes
 * gerados pelo Spring Cloud Contract encontram esta classe subindo pelos pacotes.
 */
@SpringBootApplication
public class AplicacaoDeFrete {

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoDeFrete.class, args);
    }
}
