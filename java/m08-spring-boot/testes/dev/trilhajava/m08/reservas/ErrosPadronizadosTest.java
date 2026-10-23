package dev.trilhajava.m08.reservas;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ErrosPadronizadosTest.ControladorQueFalha.class)
@DisplayName("Exercício 08.03 — Erros padronizados com Problem Details (RFC 9457)")
class ErrosPadronizadosTest {

    private static final String BASE = "https://api.hotel.dev/problemas/";
    private static final AtomicLong QUARTOS = new AtomicLong(100);

    /** Um endpoint que só existe no teste, para simular uma falha inesperada com dado sensível. */
    @RestController
    static class ControladorQueFalha {
        @GetMapping("/teste/falha")
        String falhar() {
            throw new IllegalStateException("Falha ao conectar: senha do banco = s3cr3t");
        }
    }

    @Autowired
    MockMvcTester mvc;

    private static LocalDate hoje() {
        return LocalDate.now();
    }

    private static String reserva(Long quarto, String hospede, LocalDate checkIn, LocalDate checkOut) {
        return """
                {"quartoId": %s, "hospede": %s, "checkIn": %s, "checkOut": %s}
                """.formatted(quarto, hospede == null ? "null" : "\"" + hospede + "\"",
                checkIn == null ? "null" : "\"" + checkIn + "\"", checkOut == null ? "null" : "\"" + checkOut + "\"");
    }

    private MvcTestResult post(String json) {
        return mvc.post().uri("/reservas").contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private static DocumentContext json(MvcTestResult r) {
        return JsonPath.parse(new String(r.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    /** Verifica os campos obrigatórios de um Problem Details e devolve o JSON para verificações extras. */
    private static DocumentContext problema(MvcTestResult r, HttpStatus status, String tipo, String titulo) {
        assertThat(r).hasStatus(status);
        assertThat(r).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        DocumentContext corpo = json(r);
        assertThat(corpo.read("$.type", String.class)).as("type").isEqualTo(BASE + tipo);
        assertThat(corpo.read("$.title", String.class)).as("title").isEqualTo(titulo);
        assertThat(corpo.read("$.status", Integer.class)).as("status no corpo").isEqualTo(status.value());
        return corpo;
    }

    @Test
    @DisplayName("404 reserva-nao-encontrada, com detail e instance")
    void naoEncontrada() {
        var r = mvc.get().uri("/reservas/{id}", 987654).exchange();
        DocumentContext corpo = problema(r, HttpStatus.NOT_FOUND, "reserva-nao-encontrada", "Reserva não encontrada");
        assertThat(corpo.read("$.detail", String.class)).isEqualTo("Reserva 987654 não existe");
        assertThat(corpo.read("$.instance", String.class)).isEqualTo("/reservas/987654");
    }

    @Test
    @DisplayName("400 dados-invalidos, com a lista de erros por campo em ordem alfabética")
    void validacao() {
        var r = post("{}");
        DocumentContext corpo = problema(r, HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos");
        List<Map<String, String>> erros = corpo.read("$.erros");
        assertThat(erros).containsExactly(
                Map.of("campo", "checkIn", "mensagem", "check-in é obrigatório"),
                Map.of("campo", "checkOut", "mensagem", "check-out é obrigatório"),
                Map.of("campo", "hospede", "mensagem", "hóspede é obrigatório"),
                Map.of("campo", "quartoId", "mensagem", "quarto é obrigatório"));
    }

    @Test
    @DisplayName("400 dados-invalidos também para regras como @FutureOrPresent")
    void validacaoDeData() {
        var r = post(reserva(1L, "Ana", hoje().minusDays(1), hoje().plusDays(2)));
        DocumentContext corpo = problema(r, HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos");
        assertThat(corpo.read("$.erros[*].campo", List.class)).containsExactly("checkIn");
    }

    @Nested
    @DisplayName("400 requisicao-malformada sem vazar detalhes do Jackson")
    class Malformada {
        @Test
        @DisplayName("JSON quebrado")
        void jsonQuebrado() {
            DocumentContext corpo = problema(post("{\"quartoId\": "), HttpStatus.BAD_REQUEST,
                    "requisicao-malformada", "Requisição malformada");
            assertThat(corpo.read("$.detail", String.class))
                    .isEqualTo("O corpo da requisição não é um JSON válido ou tem campos com tipo errado.");
        }

        @Test
        @DisplayName("campo com tipo errado")
        void tipoErrado() {
            String json = "{\"quartoId\": \"abc\", \"hospede\": \"Ana\", \"checkIn\": \"" + hoje().plusDays(1)
                    + "\", \"checkOut\": \"" + hoje().plusDays(2) + "\"}";
            DocumentContext corpo = problema(post(json), HttpStatus.BAD_REQUEST, "requisicao-malformada", "Requisição malformada");
            assertThat(corpo.read("$.detail", String.class)).doesNotContainIgnoringCase("jackson");
        }
    }

    @Test
    @DisplayName("409 quarto-indisponivel, com a extensão quartoId")
    void conflito() {
        long quarto = QUARTOS.incrementAndGet();
        assertThat(post(reserva(quarto, "Ana", hoje().plusDays(10), hoje().plusDays(13)))).hasStatus(HttpStatus.CREATED);

        var r = post(reserva(quarto, "Bruno", hoje().plusDays(12), hoje().plusDays(15)));
        DocumentContext corpo = problema(r, HttpStatus.CONFLICT, "quarto-indisponivel", "Quarto indisponível");
        assertThat(corpo.read("$.quartoId", Long.class)).isEqualTo(quarto);
        assertThat(corpo.read("$.detail", String.class)).contains("quarto " + quarto);
    }

    @Test
    @DisplayName("422 periodo-invalido para regra de negócio violada")
    void periodoInvalido() {
        long quarto = QUARTOS.incrementAndGet();
        var r = post(reserva(quarto, "Ana", hoje().plusDays(5), hoje().plusDays(5)));
        DocumentContext corpo = problema(r, HttpStatus.UNPROCESSABLE_CONTENT, "periodo-invalido", "Período inválido");
        assertThat(corpo.read("$.detail", String.class)).isEqualTo("o check-out deve ser depois do check-in");
    }

    @Test
    @DisplayName("500 erro-interno: não vaza a mensagem original e traz um codigoRastreio")
    void inesperado() {
        var r = mvc.get().uri("/teste/falha").exchange();
        DocumentContext corpo = problema(r, HttpStatus.INTERNAL_SERVER_ERROR, "erro-interno", "Erro interno");
        assertThat(corpo.jsonString()).doesNotContain("s3cr3t").doesNotContain("IllegalStateException");
        assertThat(corpo.read("$.codigoRastreio", String.class))
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Nested
    @DisplayName("os erros do próprio Spring MVC também saem como application/problem+json")
    class ErrosDoFramework {
        @Test
        @DisplayName("405 método não permitido, com o header Allow")
        void metodoNaoPermitido() {
            var r = mvc.put().uri("/reservas").contentType(MediaType.APPLICATION_JSON).content("{}").exchange();
            assertThat(r).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(r).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
            assertThat(r.getResponse().getHeader("Allow")).contains("POST");
        }

        @Test
        @DisplayName("400 para parâmetro de caminho com tipo errado")
        void tipoDoCaminho() {
            var r = mvc.get().uri("/reservas/abc").exchange();
            assertThat(r).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(r).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        }

        @Test
        @DisplayName("404 para rota inexistente")
        void rotaInexistente() {
            var r = mvc.get().uri("/rota-que-nao-existe").exchange();
            assertThat(r).hasStatus(HttpStatus.NOT_FOUND);
            assertThat(r).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        }
    }

    @Test
    @DisplayName("o caminho feliz continua funcionando: 201 com a reserva")
    void caminhoFeliz() {
        long quarto = QUARTOS.incrementAndGet();
        var r = post(reserva(quarto, "Carla", hoje().plusDays(20), hoje().plusDays(22)));
        assertThat(r).hasStatus(HttpStatus.CREATED);
        assertThat(r).hasContentTypeCompatibleWith(MediaType.APPLICATION_JSON);
        long id = json(r).read("$.id", Long.class);
        var cancelada = mvc.delete().uri("/reservas/{id}", id).exchange();
        assertThat(cancelada).hasStatus(HttpStatus.NO_CONTENT);
        problema(mvc.get().uri("/reservas/{id}", id).exchange(), HttpStatus.NOT_FOUND,
                "reserva-nao-encontrada", "Reserva não encontrada");
    }
}
