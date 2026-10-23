package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * O jeito anterior ao @ServiceConnection, ainda útil para serviços que o Spring Boot não
 * conhece: copiar do container para as propriedades da aplicação, uma a uma.
 */
@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
@DisplayName("Container com @DynamicPropertySource")
class ContainerComPropriedadesTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @DynamicPropertySource
    static void propriedadesDoBanco(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", postgres::getJdbcUrl); // porta sorteada: só se sabe depois do start
        registro.add("spring.datasource.username", postgres::getUsername);
        registro.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ItensDoCarrinho carrinho;

    @Test
    @DisplayName("a URL do banco aponta para uma porta sorteada, não para a 5432")
    void portaSorteada() {
        assertThat(postgres.getJdbcUrl()).startsWith("jdbc:postgresql://localhost:").doesNotContain(":5432/");
        assertThat(postgres.getMappedPort(5432)).isNotEqualTo(5432);

        UUID id = UUID.randomUUID();
        carrinho.adicionar(id, "FILTRO-102", 3);
        assertThat(carrinho.itens(id)).containsExactly(new ItemDoCarrinho("FILTRO-102", 3));
    }
}
