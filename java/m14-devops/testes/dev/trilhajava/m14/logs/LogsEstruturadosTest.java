package dev.trilhajava.m14.logs;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest(classes = LogsApplication.class, properties = "logging.level.dev.trilhajava=INFO")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("Exercício 14.01 — logs estruturados com id de correlação")
class LogsEstruturadosTest {

    static final String CPF = "52998224725";

    @Autowired
    MockMvcTester mvc;

    MvcTestResult criar(String requestId) {
        var requisicao = mvc.post().uri("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cliente\": \"Ana\", \"cpf\": \"%s\", \"valor\": 259.90}".formatted(CPF));
        if (requestId != null) {
            requisicao = requisicao.header("X-Request-Id", requestId);
        }
        return requisicao.exchange();
    }

    static String corpo(MvcTestResult r) {
        try {
            return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /** As linhas do log que são JSON e contêm o texto pedido. */
    static List<Map<String, Object>> linhasJson(CapturedOutput saida, String contendo) {
        return saida.getAll().lines()
                .filter(l -> l.startsWith("{") && l.contains(contendo))
                .map(l -> JsonPath.parse(l).<Map<String, Object>>read("$"))
                .toList();
    }

    @Test
    @DisplayName("sem X-Request-Id, a resposta ganha um id novo (um UUID)")
    void geraId() {
        MvcTestResult r = criar(null);

        assertThat(r).hasStatus(HttpStatus.CREATED);
        String id = r.getResponse().getHeader("X-Request-Id");
        assertThat(id).isNotBlank();
        assertThat(UUID.fromString(id)).isNotNull();
    }

    @Test
    @DisplayName("um X-Request-Id válido é mantido (o id atravessa os sistemas)")
    void mantemId() {
        assertThat(criar("gateway-7f3a9c").getResponse().getHeader("X-Request-Id")).isEqualTo("gateway-7f3a9c");
    }

    @Test
    @DisplayName("um X-Request-Id inválido (espaço, quebra de linha, gigante) é trocado por um novo")
    void recusaIdInvalido() {
        for (String ruim : List.of("com espaço", "a".repeat(65), "x\" ,\"nivel\":\"forjado")) {
            String id = criar(ruim).getResponse().getHeader("X-Request-Id");
            assertThat(id).as("para %s", ruim).isNotEqualTo(ruim);
            assertThat(UUID.fromString(id)).isNotNull();
        }
    }

    @Test
    @DisplayName("os logs saem em JSON (ECS), com o requestId em cada linha da requisição")
    void jsonComRequestId(CapturedOutput saida) {
        criar("req-json-01");

        List<Map<String, Object>> linhas = linhasJson(saida, "req-json-01");
        assertThat(linhas).isNotEmpty();
        Map<String, Object> linha = linhas.getFirst();
        assertThat(linha).containsKeys("@timestamp", "message");
        assertThat(linha.get("requestId")).isEqualTo("req-json-01");
        assertThat(JsonPath.parse(linha).read("$.log.level", String.class)).isEqualTo("INFO");
    }

    @Test
    @DisplayName("o log de pedido criado tem campos estruturados: pedido e valor")
    void camposEstruturados(CapturedOutput saida) {
        MvcTestResult r = criar("req-campos-01");
        String pedido = JsonPath.read(corpo(r), "$.id");

        Map<String, Object> linha = linhasJson(saida, "req-campos-01").stream()
                .filter(l -> "pedido criado".equals(l.get("message"))).findFirst().orElseThrow();
        assertThat(linha.get("pedido")).isEqualTo(pedido);
        assertThat(new java.math.BigDecimal(linha.get("valor").toString())).isEqualByComparingTo("259.90");
    }

    @Test
    @DisplayName("o CPF nunca aparece inteiro nos logs, só mascarado")
    void semCpf(CapturedOutput saida) {
        criar("req-cpf-01");

        assertThat(saida.getAll()).doesNotContain(CPF);
        Map<String, Object> linha = linhasJson(saida, "req-cpf-01").stream()
                .filter(l -> "pedido criado".equals(l.get("message"))).findFirst().orElseThrow();
        assertThat(linha.get("cpf")).isEqualTo("***.***.*47-25");
    }

    @Test
    @DisplayName("erros levam o requestId no corpo (Problem Details)")
    void erroComRequestId() {
        MvcTestResult r = mvc.get().uri("/pedidos/{id}", UUID.randomUUID()).header("X-Request-Id", "req-erro-01").exchange();

        assertThat(r).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(JsonPath.<String>read(corpo(r), "$.requestId")).isEqualTo("req-erro-01");
    }

    @Test
    @DisplayName("o MDC é limpo no fim da requisição (a thread não leva o id para a próxima)")
    void limpaMdc() {
        MvcTestResult r = criar("req-mdc-01");

        assertThat(r.getResponse().getHeader("X-Request-Id")).isEqualTo("req-mdc-01");
        assertThat(MDC.get("requestId")).isNull();
    }
}
