package dev.trilhajava.m11.aulas.defesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = "conta.jwt.segredo=segredo-so-para-teste-com-mais-de-32-bytes")
@AutoConfigureMockMvc
class FiltroDeCotaTest {

    static final RelogioAjustavel RELOGIO = new RelogioAjustavel(Instant.parse("2026-10-05T12:00:00Z"));

    @TestBean
    Clock relogio;

    static Clock relogio() {
        return RELOGIO;
    }

    @Autowired
    MockMvcTester mvc;

    @BeforeEach
    void encherTodosOsBaldes() {
        RELOGIO.avancar(Duration.ofHours(1));      // uma hora depois, todo balde do teste anterior está cheio
    }

    @Test
    void usuarioQueEsgotaACotaRecebe429ComRetryAfterEProblemDetails() {
        for (int i = 0; i < 60; i++) {
            assertThat(extratoDe("ana")).hasStatusOk();
        }

        MvcTestResult resposta = extratoDe("ana");

        assertThat(resposta).hasStatus(HttpStatus.TOO_MANY_REQUESTS)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .hasHeader("Retry-After", "1")
                .hasHeader("RateLimit-Policy", "\"por-cliente\";q=60;w=60")
                .hasHeader("RateLimit", "\"por-cliente\";r=0;t=60");
        assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                {
                  "type": "https://api.contadigital.dev/problemas/cota-excedida",
                  "title": "Cota de requisições excedida",
                  "status": 429,
                  "detail": "Cota de 60 créditos por minuto esgotada. Tente de novo em 1 s.",
                  "instance": "/api/extratos"
                }""");
    }

    @Test
    void cadaUsuarioTemOSeuBalde() {
        for (int i = 0; i < 60; i++) {
            extratoDe("ana");
        }

        assertThat(extratoDe("ana")).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(extratoDe("bruno")).hasStatusOk()
                .hasHeader("RateLimit", "\"por-cliente\";r=59;t=1");
    }

    @Test
    void exportarCustaDezCreditos() {
        for (int i = 1; i <= 6; i++) {
            assertThat(exportar("ana", "chave-" + i)).hasStatus(HttpStatus.ACCEPTED);
        }

        assertThat(exportar("ana", "chave-7")).hasStatus(HttpStatus.TOO_MANY_REQUESTS)
                .hasHeader("Retry-After", "10");
        assertThat(extratoDe("ana")).hasStatus(HttpStatus.TOO_MANY_REQUESTS)
                .hasHeader("Retry-After", "1");
    }

    @Test
    void rotaPublicaUsaOIpComoChave() {
        MvcTestResult resposta = mvc.get().uri("/api/cotacoes").with(ip("203.0.113.7")).exchange();

        assertThat(resposta).hasStatusOk()
                .hasHeader("RateLimit", "\"por-cliente\";r=59;t=1");
        assertThat(mvc.get().uri("/api/cotacoes").with(ip("198.51.100.20")).exchange())
                .hasHeader("RateLimit", "\"por-cliente\";r=59;t=1");
    }

    private MvcTestResult extratoDe(String usuario) {
        return mvc.get().uri("/api/extratos").with(jwt().jwt(token -> token.subject(usuario))).exchange();
    }

    private MvcTestResult exportar(String usuario, String chave) {
        return mvc.post().uri("/api/exportacoes")
                .with(jwt().jwt(token -> token.subject(usuario)))
                .header("Idempotency-Key", chave)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"de": "2026-09-01", "ate": "2026-09-30", "formato": "CSV"}""")
                .exchange();
    }

    private static RequestPostProcessor ip(String endereco) {
        return requisicao -> {
            requisicao.setRemoteAddr(endereco);
            return requisicao;
        };
    }
}
