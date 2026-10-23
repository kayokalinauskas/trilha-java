package dev.trilhajava.fintech;

import static dev.trilhajava.fintech.ApiDeTeste.cpf;
import static dev.trilhajava.fintech.ApiDeTeste.novaConta;
import static dev.trilhajava.fintech.ApiDeTeste.valor;
import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.fintech.ApiDeTeste.Resposta;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.web.server.LocalServerPort;

@TesteDeAceitacao
@DisplayName("Fintech Core — Marco 1: API de contas")
class Marco1ContasTest {

    static final String PROBLEMAS = "https://fintech.dev/problemas/";

    @LocalServerPort
    int porta;

    ApiDeTeste api;

    @BeforeEach
    void cliente() {
        api = ApiDeTeste.na(porta);
    }

    static void problema(Resposta r, int status, String tipo) {
        assertThat(r.status()).as("status de " + r).isEqualTo(status);
        assertThat(r.header("Content-Type")).as("Content-Type").hasValueSatisfying(
                ct -> assertThat(ct).startsWith("application/problem+json"));
        assertThat(r.ler("$.type", String.class)).as("type").isEqualTo(PROBLEMAS + tipo);
    }

    @Nested
    @DisplayName("abertura de conta")
    class Abertura {

        @Test
        @DisplayName("POST /contas → 201, Location e a conta com saldo zero e CPF mascarado")
        void abre() {
            String cpf = cpf();
            Resposta r = api.post("/contas", novaConta("Ana Souza", cpf, "CORRENTE"));

            assertThat(r.status()).as(r.toString()).isEqualTo(201);
            String id = r.ler("$.id", String.class);
            assertThat(UUID.fromString(id)).as("id é um UUID").isNotNull();
            assertThat(r.header("Location")).hasValueSatisfying(l -> assertThat(l).endsWith("/contas/" + id));
            assertThat(r.ler("$.titular", String.class)).isEqualTo("Ana Souza");
            assertThat(r.ler("$.tipo", String.class)).isEqualTo("CORRENTE");
            assertThat(r.ler("$.cpf", String.class)).isEqualTo("***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**");
            assertThat(r.decimal("$.saldo")).isEqualByComparingTo("0");
            assertThat(Instant.parse(r.ler("$.criadaEm", String.class))).isBefore(Instant.now().plusSeconds(5));
        }

        @Test
        @DisplayName("GET /contas/{id} devolve a conta")
        void consulta() {
            String id = api.abrirConta("Bruno Lima");
            Resposta r = api.get("/contas/" + id);
            assertThat(r.status()).isEqualTo(200);
            assertThat(r.ler("$.id", String.class)).isEqualTo(id);
            assertThat(r.ler("$.titular", String.class)).isEqualTo("Bruno Lima");
        }

        @Test
        @DisplayName("dados inválidos → 400 dados-invalidos com a lista de campos")
        void invalida() {
            Resposta r = api.post("/contas", novaConta("", "12345678900", "CORRENTE"));
            problema(r, 400, "dados-invalidos");
            assertThat(r.json().read("$.erros[*].campo", List.class)).contains("titular", "cpf");
        }

        @Test
        @DisplayName("tipo de conta desconhecido ou JSON malformado → 400")
        void malformada() {
            assertThat(api.post("/contas", novaConta("Ana", cpf(), "INVESTIMENTO")).status()).isEqualTo(400);
            problema(api.post("/contas", "{\"titular\": "), 400, "requisicao-malformada");
        }

        @Test
        @DisplayName("a mesma pessoa pode ter corrente e poupança, mas não duas do mesmo tipo (409)")
        void duplicada() {
            String cpf = cpf();
            assertThat(api.post("/contas", novaConta("Carla", cpf, "CORRENTE")).status()).isEqualTo(201);
            assertThat(api.post("/contas", novaConta("Carla", cpf, "POUPANCA")).status()).isEqualTo(201);
            problema(api.post("/contas", novaConta("Carla", cpf, "CORRENTE")), 409, "conta-duplicada");
        }

        @Test
        @DisplayName("conta inexistente → 404 conta-nao-encontrada; id que não é UUID → 400")
        void inexistente() {
            problema(api.get("/contas/" + UUID.randomUUID()), 404, "conta-nao-encontrada");
            assertThat(api.get("/contas/nao-e-um-uuid").status()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("movimentação")
    class Movimentacao {

        @Test
        @DisplayName("depósitos e saques atualizam o saldo e aparecem no extrato, em ordem")
        void extrato() {
            String id = api.abrirConta("Davi");
            Resposta deposito = api.post("/contas/" + id + "/depositos", valor("150.50"));
            assertThat(deposito.status()).as(deposito.toString()).isEqualTo(200);
            assertThat(deposito.decimal("$.saldo")).isEqualByComparingTo("150.50");

            assertThat(api.post("/contas/" + id + "/saques", valor("50.25")).decimal("$.saldo")).isEqualByComparingTo("100.25");

            Resposta extrato = api.get("/contas/" + id + "/extrato");
            assertThat(extrato.status()).isEqualTo(200);
            assertThat(extrato.decimal("$.saldo")).isEqualByComparingTo("100.25");
            assertThat(extrato.json().read("$.movimentos[*].tipo", List.class)).containsExactly("DEPOSITO", "SAQUE");
            assertThat(extrato.decimal("$.movimentos[0].valor")).isEqualByComparingTo("150.50");
            assertThat(extrato.decimal("$.movimentos[1].valor")).isEqualByComparingTo("50.25");
            assertThat(extrato.decimal("$.movimentos[1].saldoApos")).isEqualByComparingTo("100.25");
            assertThat(extrato.ler("$.movimentos[0].dataHora", String.class)).isNotBlank();
        }

        @Test
        @DisplayName("saque maior que o saldo → 422 saldo-insuficiente, e o saldo não muda")
        void saldoInsuficiente() {
            String id = api.abrirContaComSaldo("Elis", "100.00");
            Resposta r = api.post("/contas/" + id + "/saques", valor("100.01"));
            problema(r, 422, "saldo-insuficiente");
            assertThat(r.decimal("$.saldoDisponivel")).isEqualByComparingTo("100.00");
            assertThat(api.get("/contas/" + id).decimal("$.saldo")).isEqualByComparingTo("100.00");
        }

        @ParameterizedTest(name = "valor {0} → 400")
        @ValueSource(strings = {"0", "-10", "10.001", "null"})
        @DisplayName("valores inválidos → 400")
        void valoresInvalidos(String valor) {
            String id = api.abrirConta("Fábio");
            assertThat(api.post("/contas/" + id + "/depositos", valor(valor)).status()).isEqualTo(400);
            assertThat(api.post("/contas/" + id + "/saques", valor(valor)).status()).isEqualTo(400);
        }

        @Test
        @DisplayName("depósito em conta inexistente → 404")
        void depositoSemConta() {
            problema(api.post("/contas/" + UUID.randomUUID() + "/depositos", valor("10")), 404, "conta-nao-encontrada");
        }

        @Test
        @DisplayName("50 depósitos simultâneos de R$ 10,00 → saldo exato de R$ 500,00")
        void concorrencia() throws Exception {
            String id = api.abrirConta("Gil");
            try (var executor = Executors.newFixedThreadPool(16)) {
                var tarefas = IntStream.range(0, 50)
                        .mapToObj(i -> executor.submit(() -> api.post("/contas/" + id + "/depositos", valor("10.00"))))
                        .toList();
                for (var tarefa : tarefas) {
                    assertThat(tarefa.get().status()).isEqualTo(200);
                }
            }
            Resposta extrato = api.get("/contas/" + id + "/extrato");
            assertThat(extrato.decimal("$.saldo")).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(extrato.json().read("$.movimentos.length()", Integer.class)).isEqualTo(50);
        }
    }

    @Test
    @DisplayName("GET /contas?cpf= lista as contas de uma pessoa, paginadas")
    void listagem() {
        String cpf = cpf();
        api.post("/contas", novaConta("Helena", cpf, "CORRENTE"));
        api.post("/contas", novaConta("Helena", cpf, "POUPANCA"));

        Resposta r = api.get("/contas?cpf=" + cpf + "&pagina=0&tamanho=1");
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.json().read("$.itens.length()", Integer.class)).isEqualTo(1);
        assertThat(r.ler("$.totalItens", Integer.class)).isEqualTo(2);
        assertThat(r.ler("$.totalPaginas", Integer.class)).isEqualTo(2);
        assertThat(api.get("/contas?tamanho=500").status()).isEqualTo(400);
    }

    @Test
    @DisplayName("a API é documentada com OpenAPI em /v3/api-docs")
    void openApi() {
        Resposta r = ApiDeTeste.anonimo(porta).get("/v3/api-docs");
        assertThat(r.status()).isEqualTo(200);
        assertThat(r.json().read("$.paths", java.util.Map.class)).containsKeys(
                "/contas", "/contas/{id}", "/contas/{id}/depositos", "/contas/{id}/saques", "/contas/{id}/extrato");
    }
}
