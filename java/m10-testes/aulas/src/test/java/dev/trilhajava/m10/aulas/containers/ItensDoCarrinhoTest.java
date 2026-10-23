package dev.trilhajava.m10.aulas.containers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

@SpringBootTest
@Import(PostgresDeTeste.class)
@EnabledIfDockerAvailable // sem Docker, os testes são PULADOS (e não "aprovados")
@DisplayName("Itens do carrinho no PostgreSQL")
class ItensDoCarrinhoTest {

    static final UUID CARRINHO = UUID.fromString("00000000-0000-0000-0000-000000000042");

    @Autowired
    ItensDoCarrinho carrinho;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void limparTabela() {
        jdbc.sql("TRUNCATE item_carrinho").update(); // cada teste começa do zero
    }

    @Test
    @DisplayName("o teste roda num PostgreSQL 18 de verdade, com a migration do Flyway aplicada")
    void bancoDeVerdade() throws SQLException {
        try (Connection conexao = dataSource.getConnection()) {
            assertThat(conexao.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            assertThat(conexao.getMetaData().getDatabaseMajorVersion()).isEqualTo(18);
        }
        String migracao = jdbc.sql("SELECT description FROM flyway_schema_history WHERE version = '1'")
                .query(String.class)
                .single();
        assertThat(migracao).isEqualTo("cria item carrinho");
    }

    @Test
    @DisplayName("adicionar o mesmo produto duas vezes soma as quantidades")
    void somaAsQuantidades() {
        carrinho.adicionar(CARRINHO, "CAFE-1KG", 2);
        carrinho.adicionar(CARRINHO, "CAFE-1KG", 1);

        assertThat(carrinho.itens(CARRINHO)).containsExactly(new ItemDoCarrinho("CAFE-1KG", 3));
    }

    @Test
    @DisplayName("itens vêm em ordem de SKU, e cada carrinho só vê os seus")
    void ordemEIsolamentoEntreCarrinhos() {
        UUID outroCarrinho = UUID.randomUUID();
        carrinho.adicionar(CARRINHO, "FILTRO-102", 1);
        carrinho.adicionar(CARRINHO, "CAFE-1KG", 2);
        carrinho.adicionar(outroCarrinho, "ACUCAR-1KG", 5);

        assertThat(carrinho.itens(CARRINHO))
                .containsExactly(new ItemDoCarrinho("CAFE-1KG", 2), new ItemDoCarrinho("FILTRO-102", 1));
    }

    @Test
    @DisplayName("quantidade zero viola o CHECK da tabela")
    void quantidadeZero() {
        assertThatThrownBy(() -> carrinho.adicionar(CARRINHO, "CAFE-1KG", 0))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("item_carrinho_quantidade_check");
    }

    @Test
    @DisplayName("remover tira só aquele produto")
    void remover() {
        carrinho.adicionar(CARRINHO, "CAFE-1KG", 2);
        carrinho.adicionar(CARRINHO, "FILTRO-102", 1);

        carrinho.remover(CARRINHO, "CAFE-1KG");

        assertThat(carrinho.itens(CARRINHO)).containsExactly(new ItemDoCarrinho("FILTRO-102", 1));
    }

    @Test
    @DisplayName("8 threads adicionando o mesmo produto ao mesmo tempo: nenhuma unidade se perde")
    void concorrencia() throws Exception {
        var largada = new CountDownLatch(1);
        List<Future<?>> tarefas = new ArrayList<>();
        try (var executor = Executors.newFixedThreadPool(8)) {
            for (int thread = 0; thread < 8; thread++) {
                tarefas.add(executor.submit(() -> {
                    largada.await(); // todas começam juntas
                    for (int i = 0; i < 25; i++) {
                        carrinho.adicionar(CARRINHO, "CAFE-1KG", 1);
                    }
                    return null;
                }));
            }
            largada.countDown();
            for (Future<?> tarefa : tarefas) {
                tarefa.get(); // repassa qualquer exceção de dentro da thread
            }
        }

        assertThat(carrinho.itens(CARRINHO)).containsExactly(new ItemDoCarrinho("CAFE-1KG", 200));
    }
}
