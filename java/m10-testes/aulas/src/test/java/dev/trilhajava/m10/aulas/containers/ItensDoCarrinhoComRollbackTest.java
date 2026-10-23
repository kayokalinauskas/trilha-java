package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Outra estratégia de isolamento: cada teste roda dentro de uma transação que o Spring
 * DESFAZ (rollback) no final. Nada precisa ser limpo. A ordem fixa existe só para
 * demonstrar que o segundo teste não vê o que o primeiro gravou.
 */
@SpringBootTest
@Import(PostgresDeTeste.class) // mesma configuração dos outros testes: reaproveita contexto e container
@EnabledIfDockerAvailable
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Itens do carrinho, isolados por rollback")
class ItensDoCarrinhoComRollbackTest {

    static final UUID CARRINHO = UUID.fromString("00000000-0000-0000-0000-000000000077");

    @Autowired
    ItensDoCarrinho carrinho;

    @Autowired
    JdbcClient jdbc;

    @Test
    @Order(1)
    @DisplayName("grava dois produtos (e o Spring desfaz tudo no fim do teste)")
    void gravaDentroDaTransacao() {
        carrinho.adicionar(CARRINHO, "CAFE-1KG", 2);
        carrinho.adicionar(CARRINHO, "FILTRO-102", 1);

        assertThat(carrinho.itens(CARRINHO)).hasSize(2);
    }

    @Test
    @Order(2)
    @DisplayName("o teste seguinte encontra o carrinho vazio")
    void naoVeODoTesteAnterior() {
        assertThat(carrinho.itens(CARRINHO)).isEmpty();
        assertThat(jdbc.sql("SELECT count(*) FROM item_carrinho WHERE carrinho_id = :c")
                .param("c", CARRINHO)
                .query(Long.class)
                .single()).isZero();
    }
}
