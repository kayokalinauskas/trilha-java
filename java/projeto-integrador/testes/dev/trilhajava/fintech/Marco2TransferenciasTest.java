package dev.trilhajava.fintech;

import static dev.trilhajava.fintech.ApiDeTeste.valor;
import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.fintech.ApiDeTeste.Resposta;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

@TesteDeAceitacao
@DisplayName("Fintech Core — Marco 2: persistência e transferências atômicas")
class Marco2TransferenciasTest {

    static final String PROBLEMAS = "https://fintech.dev/problemas/";

    @LocalServerPort
    int porta;

    @Autowired
    DataSource dataSource;

    ApiDeTeste api;

    @BeforeEach
    void cliente() {
        api = ApiDeTeste.na(porta);
    }

    static String transferencia(String origem, String destino, String valor) {
        return """
                {"origem": "%s", "destino": "%s", "valor": %s}
                """.formatted(origem, destino, valor);
    }

    Resposta transferir(String chave, String origem, String destino, String valor) {
        return api.post("/transferencias", transferencia(origem, destino, valor), Map.of("Idempotency-Key", chave));
    }

    static String chave() {
        return UUID.randomUUID().toString();
    }

    java.math.BigDecimal saldo(String conta) {
        return api.get("/contas/" + conta).decimal("$.saldo");
    }

    static void problema(Resposta r, int status, String tipo) {
        assertThat(r.status()).as("status de " + r).isEqualTo(status);
        assertThat(r.ler("$.type", String.class)).as("type").isEqualTo(PROBLEMAS + tipo);
    }

    @Test
    @DisplayName("o schema é versionado com Flyway (tabela flyway_schema_history com migrações aplicadas)")
    void flyway() throws Exception {
        try (Connection c = dataSource.getConnection();
                ResultSet rs = c.createStatement().executeQuery(
                        "select count(*) from flyway_schema_history where success = true")) {
            rs.next();
            assertThat(rs.getInt(1)).as("migrações aplicadas").isPositive();
        }
    }

    @Nested
    @DisplayName("transferência")
    class Transferencia {

        @Test
        @DisplayName("POST /transferencias → 201 com Location; saldos e extratos das duas contas atualizados")
        void transfere() {
            String a = api.abrirContaComSaldo("Ana", "100.00");
            String b = api.abrirConta("Bruno");

            Resposta r = transferir(chave(), a, b, "30.00");

            assertThat(r.status()).as(r.toString()).isEqualTo(201);
            String id = r.ler("$.id", String.class);
            assertThat(UUID.fromString(id)).isNotNull();
            assertThat(r.header("Location")).hasValueSatisfying(l -> assertThat(l).endsWith("/transferencias/" + id));
            assertThat(r.ler("$.origem", String.class)).isEqualTo(a);
            assertThat(r.ler("$.destino", String.class)).isEqualTo(b);
            assertThat(r.decimal("$.valor")).isEqualByComparingTo("30.00");
            assertThat(r.ler("$.criadaEm", String.class)).isNotBlank();

            assertThat(saldo(a)).isEqualByComparingTo("70.00");
            assertThat(saldo(b)).isEqualByComparingTo("30.00");

            Resposta extratoA = api.get("/contas/" + a + "/extrato");
            assertThat(extratoA.json().read("$.movimentos[*].tipo", List.class)).containsExactly("DEPOSITO", "TRANSFERENCIA_ENVIADA");
            assertThat(extratoA.decimal("$.movimentos[1].valor")).isEqualByComparingTo("30.00");
            assertThat(extratoA.decimal("$.movimentos[1].saldoApos")).isEqualByComparingTo("70.00");
            Resposta extratoB = api.get("/contas/" + b + "/extrato");
            assertThat(extratoB.json().read("$.movimentos[*].tipo", List.class)).containsExactly("TRANSFERENCIA_RECEBIDA");

            Resposta consulta = api.get("/transferencias/" + id);
            assertThat(consulta.status()).isEqualTo(200);
            assertThat(consulta.decimal("$.valor")).isEqualByComparingTo("30.00");
        }

        @Test
        @DisplayName("saldo insuficiente → 422 e nada muda")
        void saldoInsuficiente() {
            String a = api.abrirContaComSaldo("Carla", "10.00");
            String b = api.abrirConta("Davi");
            problema(transferir(chave(), a, b, "10.01"), 422, "saldo-insuficiente");
            assertThat(saldo(a)).isEqualByComparingTo("10.00");
            assertThat(saldo(b)).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("conta inexistente → 404, e o débito não acontece")
        void contaInexistente() {
            String a = api.abrirContaComSaldo("Elis", "50.00");
            problema(transferir(chave(), a, UUID.randomUUID().toString(), "10"), 404, "conta-nao-encontrada");
            problema(transferir(chave(), UUID.randomUUID().toString(), a, "10"), 404, "conta-nao-encontrada");
            assertThat(saldo(a)).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("mesma conta → 422 transferencia-invalida; valor inválido → 400")
        void invalidas() {
            String a = api.abrirContaComSaldo("Fábio", "50.00");
            String b = api.abrirConta("Gil");
            problema(transferir(chave(), a, a, "10"), 422, "transferencia-invalida");
            assertThat(transferir(chave(), a, b, "0").status()).isEqualTo(400);
            assertThat(transferir(chave(), a, b, "1.001").status()).isEqualTo(400);
            assertThat(api.get("/transferencias/" + UUID.randomUUID()).status()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("idempotência (header Idempotency-Key)")
    class Idempotencia {

        @Test
        @DisplayName("sem o header → 400")
        void semChave() {
            String a = api.abrirContaComSaldo("Helena", "50.00");
            String b = api.abrirConta("Igor");
            assertThat(api.post("/transferencias", transferencia(a, b, "10"), Map.of()).status()).isEqualTo(400);
            assertThat(saldo(a)).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("repetir a mesma requisição com a mesma chave devolve a MESMA resposta e não debita de novo")
        void repeticao() {
            String a = api.abrirContaComSaldo("Joana", "100.00");
            String b = api.abrirConta("Kaio");
            String chave = chave();

            Resposta primeira = transferir(chave, a, b, "40.00");
            Resposta segunda = transferir(chave, a, b, "40.00");

            assertThat(primeira.status()).isEqualTo(201);
            assertThat(segunda.status()).as(segunda.toString()).isEqualTo(201);
            assertThat(segunda.ler("$.id", String.class)).isEqualTo(primeira.ler("$.id", String.class));
            assertThat(segunda.header("Idempotent-Replayed")).hasValue("true");
            assertThat(saldo(a)).isEqualByComparingTo("60.00");
            assertThat(saldo(b)).isEqualByComparingTo("40.00");
        }

        @Test
        @DisplayName("mesma chave com outro conteúdo → 422 chave-idempotencia-reutilizada")
        void chaveReutilizada() {
            String a = api.abrirContaComSaldo("Lia", "100.00");
            String b = api.abrirConta("Mauro");
            String chave = chave();
            assertThat(transferir(chave, a, b, "10.00").status()).isEqualTo(201);
            problema(transferir(chave, a, b, "20.00"), 422, "chave-idempotencia-reutilizada");
            assertThat(saldo(a)).isEqualByComparingTo("90.00");
        }

        @Test
        @DisplayName("a mesma requisição enviada 6 vezes AO MESMO TEMPO executa uma única transferência")
        void repeticaoConcorrente() throws Exception {
            String a = api.abrirContaComSaldo("Nina", "100.00");
            String b = api.abrirConta("Otto");
            String chave = chave();

            List<Resposta> respostas = emParalelo(6, i -> transferir(chave, a, b, "25.00"));

            assertThat(respostas).extracting(Resposta::status).allSatisfy(s -> assertThat(s).isIn(201, 409));
            Set<String> ids = respostas.stream().filter(r -> r.status() == 201)
                    .map(r -> r.ler("$.id", String.class)).collect(Collectors.toSet());
            assertThat(ids).as("todas as respostas 201 são da mesma transferência").hasSize(1);
            assertThat(saldo(a)).isEqualByComparingTo("75.00");
            assertThat(saldo(b)).isEqualByComparingTo("25.00");
        }
    }

    @Nested
    @DisplayName("concorrência")
    class Concorrencia {

        @Test
        @DisplayName("20 transferências simultâneas de R$ 10 de uma conta com R$ 100 → 10 aceitas, 10 recusadas, saldo nunca negativo")
        void mesmaOrigem() throws Exception {
            String a = api.abrirContaComSaldo("Paula", "100.00");
            String b = api.abrirConta("Quim");

            List<Resposta> respostas = emParalelo(20, i -> transferir(chave(), a, b, "10.00"));

            assertThat(respostas.stream().filter(r -> r.status() == 201)).hasSize(10);
            assertThat(respostas.stream().filter(r -> r.status() == 422)).hasSize(10);
            assertThat(saldo(a)).isEqualByComparingTo("0.00");
            assertThat(saldo(b)).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("transferências em sentidos opostos ao mesmo tempo (A→B e B→A) terminam sem deadlock nem erro 500")
        void sentidosOpostos() throws Exception {
            String a = api.abrirContaComSaldo("Rui", "100.00");
            String b = api.abrirContaComSaldo("Sara", "100.00");

            List<Resposta> respostas = emParalelo(20, i -> i % 2 == 0
                    ? transferir(chave(), a, b, "5.00")
                    : transferir(chave(), b, a, "5.00"));

            assertThat(respostas).extracting(Resposta::status).containsOnly(201);
            assertThat(saldo(a)).isEqualByComparingTo("100.00");
            assertThat(saldo(b)).isEqualByComparingTo("100.00");
        }
    }

    List<Resposta> emParalelo(int quantidade, java.util.function.IntFunction<Resposta> chamada) throws Exception {
        var largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(quantidade)) {
            List<Future<Resposta>> tarefas = new ArrayList<>();
            for (int i = 0; i < quantidade; i++) {
                int n = i;
                tarefas.add(executor.submit(() -> {
                    largada.await();
                    return chamada.apply(n);
                }));
            }
            largada.countDown();
            List<Resposta> respostas = new ArrayList<>();
            for (Future<Resposta> t : tarefas) {
                respostas.add(t.get());
            }
            return respostas;
        }
    }
}
