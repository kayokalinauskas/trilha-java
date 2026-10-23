package dev.trilhajava.fintech;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import dev.trilhajava.fintech.ApiDeTeste.Resposta;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

/**
 * Marco 6: a aplicação pronta para produção — health checks, métricas, id de correlação e logs em JSON.
 * Tudo verificado por HTTP e pela saída do console, como um operador veria. O contrato está no enunciado.
 */
@TesteDeAceitacao
@TestPropertySource(properties = "logging.level.dev.trilhajava=INFO")
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Fintech Core — Marco 6: pronto para produção (Actuator, métricas, logs, X-Request-Id)")
class Marco6ProducaoTest {

    @LocalServerPort
    int porta;

    @Autowired
    DataSource dataSource;

    ApiDeTeste admin;
    ApiDeTeste anonimo;

    @BeforeEach
    void clientes() {
        admin = ApiDeTeste.na(porta);
        anonimo = ApiDeTeste.anonimo(porta);
    }

    // ------------------------------------------------------------------ auxiliares

    Resposta get(ApiDeTeste api, String caminho, String requestId) {
        return api.enviar("GET", caminho, null, requestId == null ? Map.of() : Map.of("X-Request-Id", requestId));
    }

    Resposta transferir(String origem, String destino, String valor, String requestId) {
        return transferir(origem, destino, valor, requestId, UUID.randomUUID().toString());
    }

    Resposta transferir(String origem, String destino, String valor, String requestId, String chave) {
        return admin.post("/transferencias",
                "{\"origem\": \"%s\", \"destino\": \"%s\", \"valor\": %s}".formatted(origem, destino, valor),
                Map.of("Idempotency-Key", chave, "X-Request-Id", requestId));
    }

    /** Um cliente comum (papel USER), cadastrado agora. */
    ApiDeTeste usuarioComum() {
        String email = "comum-" + UUID.randomUUID().toString().substring(0, 8) + "@exemplo.com";
        Resposta cadastro = anonimo.post("/usuarios",
                "{\"nome\": \"Comum\", \"email\": \"%s\", \"senha\": \"senha-do-usuario-comum\"}".formatted(email));
        assertThat(cadastro.status()).as(cadastro.toString()).isEqualTo(201);
        Resposta token = anonimo.post("/auth/token",
                "{\"email\": \"%s\", \"senha\": \"senha-do-usuario-comum\"}".formatted(email));
        assertThat(token.status()).as(token.toString()).isEqualTo(200);
        return anonimo.comToken(token.ler("$.accessToken", String.class));
    }

    /** O formato de texto do Prometheus (o cliente de teste pede JSON por padrão). */
    Resposta prometheus(ApiDeTeste api) {
        return api.enviar("GET", "/actuator/prometheus", null, Map.of("Accept", "text/plain"));
    }

    String prometheus() {
        Resposta r = prometheus(admin);
        assertThat(r.status()).as(r.toString()).isEqualTo(200);
        return r.corpo();
    }

    /** Valor de fintech_transferencias_total{resultado="..."} (0 se a série ainda não existe). */
    double transferencias(String resultado) {
        Matcher m = Pattern.compile("(?m)^fintech_transferencias_total\\{[^}]*resultado=\"" + resultado + "\"[^}]*\\} (\\S+)$")
                .matcher(prometheus());
        return m.find() ? Double.parseDouble(m.group(1)) : 0;
    }

    long pendentesNaOutbox() {
        Resposta r = get(admin, "/actuator/health", null);
        return r.ler("$.components.outbox.details.pendentes", Number.class).longValue();
    }

    /** As linhas JSON do console que contêm o texto. */
    static List<Map<String, Object>> linhasJson(CapturedOutput saida, String contendo) {
        return saida.getAll().lines()
                .filter(l -> l.startsWith("{") && l.contains(contendo))
                .map(l -> JsonPath.parse(l).<Map<String, Object>>read("$"))
                .toList();
    }

    // ------------------------------------------------------------------ health checks

    @Test
    @DisplayName("/actuator/health é público e, para anônimos, mostra só o status")
    void healthPublicoSoStatus() {
        Resposta r = get(anonimo, "/actuator/health", null);

        assertThat(r.status()).as(r.toString()).isEqualTo(200);
        assertThat(r.ler("$.status", String.class)).isEqualTo("UP");
        assertThat(r.corpo()).doesNotContain("components").doesNotContain("details");
    }

    @Test
    @DisplayName("as probes /actuator/health/liveness e /readiness respondem sem token")
    void probes() {
        for (String probe : List.of("/actuator/health/liveness", "/actuator/health/readiness")) {
            Resposta r = get(anonimo, probe, null);
            assertThat(r.status()).as(probe + " " + r).isEqualTo(200);
            assertThat(r.ler("$.status", String.class)).isEqualTo("UP");
        }
    }

    @Test
    @DisplayName("o ADMIN vê os componentes: db e outbox (com o número de eventos pendentes)")
    void adminVeComponentes() {
        Resposta r = get(admin, "/actuator/health", null);

        assertThat(r.status()).as(r.toString()).isEqualTo(200);
        assertThat(r.ler("$.components.db.status", String.class)).isEqualTo("UP");
        assertThat(r.ler("$.components.outbox.status", String.class)).isEqualTo("UP");
        assertThat(r.ler("$.components.outbox.details.pendentes", Number.class)).isNotNull();
    }

    @Test
    @DisplayName("um cliente comum não vê os componentes do health")
    void clienteComumNaoVeComponentes() {
        Resposta r = get(usuarioComum(), "/actuator/health", null);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.corpo()).doesNotContain("components");
    }

    @Test
    @DisplayName("cada transferência deixa um evento pendente na outbox (aqui, sem broker, ninguém publica)")
    void pendentesCrescem() {
        String origem = admin.abrirContaComSaldo("Ana", "100.00");
        String destino = admin.abrirConta("Bruno");
        long antes = pendentesNaOutbox();

        assertThat(transferir(origem, destino, "10.00", "marco6-pend-01").status()).isEqualTo(201);

        assertThat(pendentesNaOutbox()).isEqualTo(antes + 1);
    }

    @Test
    @DisplayName("evento parado há mais de 10 min: outbox DOWN (health 503 para o admin), mas a readiness segue UP")
    void outboxAtrasada() throws Exception {
        UUID id = UUID.randomUUID();
        Instant criado = Instant.now().minus(Duration.ofMinutes(11));
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO evento_saida (id, agregado_id, tipo, payload, criado_em, proxima_tentativa)
                        VALUES (?, ?, 'TransferenciaRealizada', '{}', ?, ?)""")) {
            ps.setObject(1, id);
            ps.setObject(2, UUID.randomUUID());
            ps.setTimestamp(3, Timestamp.from(criado));
            ps.setTimestamp(4, Timestamp.from(criado));
            ps.executeUpdate();
        }
        try {
            Resposta health = get(admin, "/actuator/health", null);
            assertThat(health.status()).as(health.toString()).isEqualTo(503);
            assertThat(health.ler("$.components.outbox.status", String.class)).isEqualTo("DOWN");
            assertThat(health.ler("$.components.db.status", String.class)).isEqualTo("UP");

            Resposta readiness = get(anonimo, "/actuator/health/readiness", null);
            assertThat(readiness.status()).as("sem Kafka a API ainda atende: " + readiness).isEqualTo(200);
        } finally {
            try (Connection c = dataSource.getConnection();
                    PreparedStatement ps = c.prepareStatement("DELETE FROM evento_saida WHERE id = ?")) {
                ps.setObject(1, id);
                ps.executeUpdate();
            }
        }
    }

    // ------------------------------------------------------------------ métricas

    @Test
    @DisplayName("/actuator/prometheus: 401 sem token, 403 para cliente comum, 200 para ADMIN")
    void prometheusSoParaAdmin() {
        assertThat(prometheus(anonimo).status()).isEqualTo(401);
        assertThat(prometheus(usuarioComum()).status()).isEqualTo(403);
        assertThat(prometheus(admin).status()).isEqualTo(200);
    }

    @Test
    @DisplayName("só health e prometheus expostos: /actuator/env não é acessível nem para o ADMIN")
    void semEndpointsSensiveis() {
        assertThat(get(admin, "/actuator/env", null).status()).isIn(403, 404);
        assertThat(get(admin, "/actuator/heapdump", null).status()).isIn(403, 404);
    }

    @Test
    @DisplayName("fintech_transferencias_total conta sucesso e saldo_insuficiente separadamente")
    void contaTransferencias() {
        String origem = admin.abrirContaComSaldo("Carla", "50.00");
        String destino = admin.abrirConta("Davi");
        double sucessos = transferencias("sucesso");
        double semSaldo = transferencias("saldo_insuficiente");

        assertThat(transferir(origem, destino, "20.00", "marco6-met-01").status()).isEqualTo(201);
        assertThat(transferir(origem, destino, "999.00", "marco6-met-02").status()).isEqualTo(422);

        assertThat(transferencias("sucesso")).isEqualTo(sucessos + 1);
        assertThat(transferencias("saldo_insuficiente")).isEqualTo(semSaldo + 1);
    }

    @Test
    @DisplayName("repetir uma transferência com a mesma Idempotency-Key não conta de novo")
    void repeticaoNaoConta() {
        String origem = admin.abrirContaComSaldo("Elis", "50.00");
        String destino = admin.abrirConta("Fabio");
        String chave = UUID.randomUUID().toString();
        assertThat(transferir(origem, destino, "5.00", "marco6-rep-01", chave).status()).isEqualTo(201);
        double depois = transferencias("sucesso");

        Resposta repetida = transferir(origem, destino, "5.00", "marco6-rep-02", chave);

        assertThat(repetida.header("Idempotent-Replayed")).contains("true");
        assertThat(transferencias("sucesso")).isEqualTo(depois);
    }

    @Test
    @DisplayName("o Prometheus recebe o gauge fintech_outbox_pendentes e a tag comum aplicacao")
    void gaugeDaOutbox() {
        String corpo = prometheus();

        assertThat(corpo).containsPattern("(?m)^fintech_outbox_pendentes\\{[^}]*\\} \\S+$");
        assertThat(corpo).contains("aplicacao=\"fintech-core\"");
    }

    // ------------------------------------------------------------------ X-Request-Id

    @Test
    @DisplayName("sem X-Request-Id, a resposta ganha um UUID; com um válido, ele é devolvido igual")
    void requestId() {
        Resposta sem = get(anonimo, "/actuator/health", null);
        assertThat(UUID.fromString(sem.header("X-Request-Id").orElseThrow())).isNotNull();

        Resposta com = get(anonimo, "/actuator/health", "gateway-7f3a9c");
        assertThat(com.header("X-Request-Id")).contains("gateway-7f3a9c");
    }

    @Test
    @DisplayName("X-Request-Id inválido (espaços, aspas, mais de 64 caracteres) é trocado por um UUID")
    void requestIdInvalido() {
        for (String ruim : List.of("com espaco e \"aspas\"", "a".repeat(65))) {
            String devolvido = get(anonimo, "/actuator/health", ruim).header("X-Request-Id").orElseThrow();
            assertThat(devolvido).isNotEqualTo(ruim);
            assertThat(UUID.fromString(devolvido)).isNotNull();
        }
    }

    @Test
    @DisplayName("erros de negócio levam o requestId no Problem Details")
    void requestIdNoErro() {
        Resposta r = get(admin, "/contas/" + UUID.randomUUID(), "marco6-erro-404");

        assertThat(r.status()).isEqualTo(404);
        assertThat(r.ler("$.requestId", String.class)).isEqualTo("marco6-erro-404");
    }

    @Test
    @DisplayName("até o 401 do Spring Security leva o X-Request-Id e o requestId")
    void requestIdNo401() {
        Resposta r = get(anonimo, "/contas/" + UUID.randomUUID(), "marco6-erro-401");

        assertThat(r.status()).isEqualTo(401);
        assertThat(r.header("X-Request-Id")).contains("marco6-erro-401");
        assertThat(r.ler("$.requestId", String.class)).isEqualTo("marco6-erro-401");
    }

    // ------------------------------------------------------------------ logs

    @Test
    @DisplayName("cada transferência gera um log JSON (INFO) com requestId, transferencia e valor")
    void logDaTransferencia(CapturedOutput saida) {
        String origem = admin.abrirContaComSaldo("Gabi", "80.00");
        String destino = admin.abrirConta("Hugo");

        Resposta r = transferir(origem, destino, "33.10", "marco6-log-01");
        assertThat(r.status()).isEqualTo(201);
        String transferencia = r.ler("$.id", String.class);

        Map<String, Object> linha = linhasJson(saida, "marco6-log-01").stream()
                .filter(l -> "transferencia realizada".equals(l.get("message")))
                .findFirst().orElseThrow(() -> new AssertionError("nenhum log JSON 'transferencia realizada' com o requestId"));
        assertThat(linha.get("requestId")).isEqualTo("marco6-log-01");
        assertThat(linha.get("transferencia")).isEqualTo(transferencia);
        assertThat(new BigDecimal(linha.get("valor").toString())).isEqualByComparingTo("33.10");
        assertThat(JsonPath.parse(linha).read("$.log.level", String.class)).isEqualTo("INFO");
    }

    @Test
    @DisplayName("transferência recusada não gera o log de transferência realizada")
    void semLogNaRecusa(CapturedOutput saida) {
        String origem = admin.abrirContaComSaldo("Iara", "1.00");
        String destino = admin.abrirConta("Joel");

        assertThat(transferir(origem, destino, "500.00", "marco6-log-02").status()).isEqualTo(422);

        assertThat(linhasJson(saida, "marco6-log-02"))
                .noneMatch(l -> "transferencia realizada".equals(l.get("message")));
    }
}
