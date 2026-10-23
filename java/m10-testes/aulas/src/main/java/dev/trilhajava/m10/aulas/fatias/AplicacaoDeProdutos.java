package dev.trilhajava.m10.aulas.fatias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * A API de produtos da aula 10.04. As fatias de teste ({@code @WebMvcTest}, {@code @DataJpaTest})
 * procuram esta classe subindo pelos pacotes a partir do pacote do teste: é ela que diz ao Spring
 * onde começa a varredura de componentes.
 */
@SpringBootApplication
@PropertySource("classpath:aulas-fatias.properties")
public class AplicacaoDeProdutos {

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoDeProdutos.class, args);
    }
}
