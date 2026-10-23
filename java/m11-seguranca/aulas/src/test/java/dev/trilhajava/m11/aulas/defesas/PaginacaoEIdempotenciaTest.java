package dev.trilhajava.m11.aulas.defesas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest(properties = "conta.jwt.segredo=segredo-so-para-teste-com-mais-de-32-bytes")
@AutoConfigureMockMvc
class PaginacaoEIdempotenciaTest {

    static final String SETEMBRO = """
            {"de": "2026-09-01", "ate": "2026-09-30", "formato": "CSV"}""";
    static final String OUTUBRO = """
            {"de": "2026-10-01", "ate": "2026-10-31", "formato": "CSV"}""";

    @Autowired
    MockMvcTester mvc;

    @Test
    void paginaDentroDoLimite() {
        MvcTestResult resposta = mvc.get().uri("/api/extratos?pagina=2&tamanho=100")
                .with(jwt().jwt(t -> t.subject("ana"))).exchange();

        assertThat(resposta).hasStatusOk();
        assertThat(resposta).bodyJson().extractingPath("$.lancamentos.length()").isEqualTo(50);
        assertThat(resposta).bodyJson().extractingPath("$.totalDeLancamentos").isEqualTo(250);
    }

    @Test
    void paginaGrandeDemaisERecusadaCom400() {
        MvcTestResult resposta = mvc.get().uri("/api/extratos?tamanho=5000")
                .with(jwt().jwt(t -> t.subject("ana"))).exchange();

        assertThat(resposta).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                {"title": "Bad Request", "status": 400, "detail": "Validation failure", "instance": "/api/extratos"}""");
    }

    @Test
    void semIdempotencyKeyERecusadoCom400() {
        MvcTestResult resposta = mvc.post().uri("/api/exportacoes")
                .with(jwt().jwt(t -> t.subject("ana")))
                .contentType(MediaType.APPLICATION_JSON).content(SETEMBRO)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(resposta).bodyJson().extractingPath("$.detail")
                .isEqualTo("Required header 'Idempotency-Key' is not present.");
    }

    @Test
    void repetirComAMesmaChaveDevolveAMesmaExportacao() {
        MvcTestResult primeira = exportar("bruno", "6f1c2a9e-0d7b-4c55-9a43-1b2f8e0c7d11", SETEMBRO);
        MvcTestResult repetida = exportar("bruno", "6f1c2a9e-0d7b-4c55-9a43-1b2f8e0c7d11", SETEMBRO);

        assertThat(primeira).hasStatus(HttpStatus.ACCEPTED).hasHeader("Idempotent-Replayed", "false");
        assertThat(repetida).hasStatus(HttpStatus.ACCEPTED).hasHeader("Idempotent-Replayed", "true");
        assertThat(repetida.getResponse().getHeader("Location"))
                .isEqualTo(primeira.getResponse().getHeader("Location"));
    }

    @Test
    void mesmaChaveComOutroCorpoERecusadaCom422() {
        exportar("carla", "a3e9c1d2-77b0-4f1e-8c2d-5e6f7a8b9c00", SETEMBRO);

        MvcTestResult resposta = exportar("carla", "a3e9c1d2-77b0-4f1e-8c2d-5e6f7a8b9c00", OUTUBRO);

        assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(resposta).bodyJson().extractingPath("$.title").isEqualTo("Idempotency-Key reutilizada");
    }

    @Test
    void aChaveValeSoParaQuemACriou() {
        MvcTestResult daDaniela = exportar("daniela", "chave-compartilhada", SETEMBRO);
        MvcTestResult doEduardo = exportar("eduardo", "chave-compartilhada", SETEMBRO);

        assertThat(doEduardo).hasHeader("Idempotent-Replayed", "false");
        assertThat(doEduardo.getResponse().getHeader("Location"))
                .isNotEqualTo(daDaniela.getResponse().getHeader("Location"));
    }

    private MvcTestResult exportar(String usuario, String chave, String corpo) {
        return mvc.post().uri("/api/exportacoes")
                .with(jwt().jwt(t -> t.subject(usuario)))
                .header("Idempotency-Key", chave)
                .contentType(MediaType.APPLICATION_JSON).content(corpo)
                .exchange();
    }
}
