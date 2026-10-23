package dev.trilhajava.m11.aulas.defesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Solução do checkpoint da aula: exportação + 45 extratos + outra exportação, com o relógio parado. */
@SpringBootTest(properties = "conta.jwt.segredo=segredo-so-para-teste-com-mais-de-32-bytes")
@AutoConfigureMockMvc
class CheckpointCotaTest {

    static final RelogioAjustavel RELOGIO = new RelogioAjustavel(Instant.parse("2026-10-05T12:00:00Z"));

    @TestBean
    Clock relogio;

    static Clock relogio() {
        return RELOGIO;
    }

    @Autowired
    MockMvcTester mvc;

    @Test
    void segundaExportacaoEsperaCincoSegundos() {
        assertThat(exportar("chave-1")).hasStatus(HttpStatus.ACCEPTED)
                .hasHeader("RateLimit", "\"por-cliente\";r=50;t=10");
        for (int i = 0; i < 45; i++) {
            mvc.get().uri("/api/extratos").with(jwt().jwt(t -> t.subject("fernanda"))).exchange();
        }

        assertThat(exportar("chave-2")).hasStatus(HttpStatus.TOO_MANY_REQUESTS)
                .hasHeader("Retry-After", "5")
                .hasHeader("RateLimit", "\"por-cliente\";r=5;t=55");

        RELOGIO.avancar(Duration.ofSeconds(5));
        assertThat(exportar("chave-2")).hasStatus(HttpStatus.ACCEPTED)
                .hasHeader("RateLimit", "\"por-cliente\";r=0;t=60");
    }

    private MvcTestResult exportar(String chave) {
        return mvc.post().uri("/api/exportacoes")
                .with(jwt().jwt(t -> t.subject("fernanda")))
                .header("Idempotency-Key", chave)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"de": "2026-09-01", "ate": "2026-09-30", "formato": "CSV"}""")
                .exchange();
    }
}
