package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * O mesmo cenário do CarrinhoV1NoH2Test, agora no PostgreSQL de verdade. Na aula, este teste
 * aparece primeiro com a asserção do H2 (e falha); aqui ele fica registrado como documentação
 * do bug: o PostgreSQL aborta a transação no erro de chave duplicada.
 */
@SpringBootTest
@Import(PostgresDeTeste.class)
@EnabledIfDockerAvailable
@DisplayName("Carrinho V1 no PostgreSQL")
class CarrinhoV1NoPostgresTest {

    @Autowired
    ItensDoCarrinhoV1 carrinho;

    @Test
    @DisplayName("o segundo adicionar do mesmo produto quebra: a transação foi abortada")
    void segundoAdicionarQuebra() {
        UUID carrinhoDaAna = UUID.randomUUID();
        carrinho.adicionar(carrinhoDaAna, "CAFE-1KG", 2);

        assertThatThrownBy(() -> carrinho.adicionar(carrinhoDaAna, "CAFE-1KG", 1))
                .hasMessageContaining("current transaction is aborted");
    }
}
