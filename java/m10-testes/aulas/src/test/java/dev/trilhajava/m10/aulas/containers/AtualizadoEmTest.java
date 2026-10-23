package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/** Solução do checkpoint da aula 10.05: o upsert também renova atualizado_em. */
@SpringBootTest
@Import(PostgresDeTeste.class) // a mesma configuração: reaproveita o contexto e o container
@EnabledIfDockerAvailable
@DisplayName("Checkpoint: atualizado_em")
class AtualizadoEmTest {

    @Autowired
    ItensDoCarrinho carrinho;

    @Autowired
    JdbcClient jdbc;

    @Test
    @DisplayName("somar a quantidade renova atualizado_em")
    void upsertRenovaAtualizadoEm() {
        UUID carrinhoNovo = UUID.randomUUID(); // dado único: não precisa limpar a tabela

        carrinho.adicionar(carrinhoNovo, "CAFE-1KG", 1);
        OffsetDateTime primeiraVez = atualizadoEm(carrinhoNovo, "CAFE-1KG");

        carrinho.adicionar(carrinhoNovo, "CAFE-1KG", 1);
        OffsetDateTime segundaVez = atualizadoEm(carrinhoNovo, "CAFE-1KG");

        assertThat(segundaVez).isAfter(primeiraVez);
        assertThat(carrinho.itens(carrinhoNovo)).containsExactly(new ItemDoCarrinho("CAFE-1KG", 2));
    }

    private OffsetDateTime atualizadoEm(UUID carrinhoId, String sku) {
        return jdbc.sql("SELECT atualizado_em FROM item_carrinho WHERE carrinho_id = :c AND sku = :s")
                .param("c", carrinhoId)
                .param("s", sku)
                .query(OffsetDateTime.class)
                .single();
    }
}
