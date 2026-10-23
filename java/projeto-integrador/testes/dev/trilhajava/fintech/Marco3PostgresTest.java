package dev.trilhajava.fintech;

import static dev.trilhajava.fintech.ApiDeTeste.cpf;
import static dev.trilhajava.fintech.ApiDeTeste.novaConta;
import static dev.trilhajava.fintech.ApiDeTeste.valor;
import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.fintech.ApiDeTeste.Resposta;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

@TesteDeAceitacao
@Import(PostgresDeTeste.class)
@EnabledIfDockerAvailable
@DisplayName("Fintech Core — Marco 3: a aplicação inteira sobre PostgreSQL de verdade")
class Marco3PostgresTest {

    @LocalServerPort
    int porta;

    @Autowired
    DataSource dataSource;

    ApiDeTeste api;

    @BeforeEach
    void cliente() {
        api = ApiDeTeste.na(porta);
    }

    Resposta transferir(String chave, String origem, String destino, String valor) {
        return api.post("/transferencias",
                "{\"origem\": \"%s\", \"destino\": \"%s\", \"valor\": %s}".formatted(origem, destino, valor),
                Map.of("Idempotency-Key", chave));
    }

    @Test
    @DisplayName("o banco é o PostgreSQL 18 e o schema foi criado pelas migrações Flyway")
    void postgres() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            assertThat(c.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            assertThat(c.getMetaData().getDatabaseMajorVersion()).isEqualTo(18);
            try (ResultSet rs = c.createStatement().executeQuery(
                    "select count(*) from flyway_schema_history where success")) {
                rs.next();
                assertThat(rs.getInt(1)).isPositive();
            }
        }
    }

    @Test
    @DisplayName("fluxo completo: abrir, depositar, transferir, repetir com a mesma chave e consultar extratos")
    void fluxoCompleto() {
        String ana = api.abrirContaComSaldo("Ana", "500.00");
        String bruno = api.abrirConta("Bruno");
        String chave = UUID.randomUUID().toString();

        Resposta primeira = transferir(chave, ana, bruno, "120.50");
        Resposta repetida = transferir(chave, ana, bruno, "120.50");

        assertThat(primeira.status()).as(primeira.toString()).isEqualTo(201);
        assertThat(repetida.ler("$.id", String.class)).isEqualTo(primeira.ler("$.id", String.class));
        assertThat(api.get("/contas/" + ana).decimal("$.saldo")).isEqualByComparingTo("379.50");
        assertThat(api.get("/contas/" + bruno + "/extrato").json().read("$.movimentos[*].tipo", List.class))
                .containsExactly("TRANSFERENCIA_RECEBIDA");
        assertThat(api.post("/contas/" + bruno + "/saques", valor("200.00")).status()).isEqualTo(422);
    }

    @Test
    @DisplayName("as constraints do PostgreSQL continuam valendo: CPF + tipo duplicado → 409")
    void duplicada() {
        String documento = cpf();
        assertThat(api.post("/contas", novaConta("Carla", documento, "CORRENTE")).status()).isEqualTo(201);
        assertThat(api.post("/contas", novaConta("Carla", documento, "CORRENTE")).status()).isEqualTo(409);
    }

    @Test
    @DisplayName("com os locks do PostgreSQL: 20 transferências simultâneas de R$ 10 com saldo de R$ 100 → 10 aceitas")
    void concorrencia() throws Exception {
        String origem = api.abrirContaComSaldo("Davi", "100.00");
        String destino = api.abrirConta("Elis");
        var largada = new CountDownLatch(1);
        List<Resposta> respostas = new ArrayList<>();
        try (var executor = Executors.newFixedThreadPool(20)) {
            List<Future<Resposta>> tarefas = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                tarefas.add(executor.submit(() -> {
                    largada.await();
                    return transferir(UUID.randomUUID().toString(), origem, destino, "10.00");
                }));
            }
            largada.countDown();
            for (Future<Resposta> t : tarefas) {
                respostas.add(t.get());
            }
        }
        assertThat(respostas.stream().filter(r -> r.status() == 201)).hasSize(10);
        assertThat(respostas.stream().filter(r -> r.status() == 422)).hasSize(10);
        assertThat(api.get("/contas/" + origem).decimal("$.saldo")).isEqualByComparingTo("0");
    }
}
