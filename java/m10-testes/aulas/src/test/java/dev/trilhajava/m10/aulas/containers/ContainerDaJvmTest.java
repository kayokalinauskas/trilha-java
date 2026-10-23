package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/** Usa o container singleton de ContainersDaJvm; o Spring o inicia e liga o DataSource a ele. */
@SpringBootTest
@ImportTestcontainers(ContainersDaJvm.class)
@EnabledIfDockerAvailable
@DisplayName("Container singleton, um por JVM")
class ContainerDaJvmTest {

    @Autowired
    ItensDoCarrinho carrinho;

    @Test
    @DisplayName("o Spring se conecta ao container compartilhado")
    void usaOContainerDaJvm() {
        UUID id = UUID.randomUUID();
        carrinho.adicionar(id, "ACUCAR-1KG", 4);

        assertThat(carrinho.itens(id)).containsExactly(new ItemDoCarrinho("ACUCAR-1KG", 4));
        assertThat(ContainersDaJvm.POSTGRES.isRunning()).isTrue();
    }
}
