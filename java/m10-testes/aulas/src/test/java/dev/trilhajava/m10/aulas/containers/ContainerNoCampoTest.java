package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * O estilo "container no campo": quem inicia e para o container é a extensão do
 * Testcontainers para o JUnit (@Testcontainers + @Container), uma vez por CLASSE.
 * O @ServiceConnection no campo continua dispensando as spring.datasource.*.
 */
@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
@DisplayName("Container declarado no campo da classe de teste")
class ContainerNoCampoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    ItensDoCarrinho carrinho;

    @Test
    @DisplayName("o Spring se conecta ao container desta classe")
    void usaOContainerDaClasse() {
        UUID id = UUID.randomUUID();
        carrinho.adicionar(id, "CAFE-1KG", 1);

        assertThat(carrinho.itens(id)).containsExactly(new ItemDoCarrinho("CAFE-1KG", 1));
        assertThat(postgres.isRunning()).isTrue();
    }
}
