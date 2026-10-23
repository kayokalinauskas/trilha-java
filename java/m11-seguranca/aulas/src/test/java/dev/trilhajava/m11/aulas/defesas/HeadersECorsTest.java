package dev.trilhajava.m11.aulas.defesas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest(properties = "conta.jwt.segredo=segredo-so-para-teste-com-mais-de-32-bytes")
@AutoConfigureMockMvc
class HeadersECorsTest {

    static final String FRONT = "https://app.contadigital.dev";

    @Autowired
    MockMvcTester mvc;

    @Test
    void headersDeSegurancaEmTodaResposta() {
        MvcTestResult resposta = mvc.get().uri("/api/cotacoes").exchange();

        assertThat(resposta).hasStatusOk()
                .hasHeader("X-Content-Type-Options", "nosniff")
                .hasHeader("X-Frame-Options", "DENY")
                .hasHeader("X-XSS-Protection", "0")
                .hasHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
                .hasHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")
                .hasHeader("Referrer-Policy", "no-referrer");
        assertThat(resposta).headers().doesNotContainHeader("Strict-Transport-Security");
    }

    @Test
    void hstsSoVemEmHttps() {
        MvcTestResult resposta = mvc.get().uri("/api/cotacoes").secure(true).exchange();

        assertThat(resposta).hasHeader("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains");
    }

    @Test
    void preflightDoFrontPassaSemTokenESemGastarCota() {
        MvcTestResult resposta = mvc.options().uri("/api/exportacoes")
                .header("Origin", FRONT)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization, content-type, idempotency-key")
                .exchange();

        assertThat(resposta).hasStatusOk()
                .hasHeader("Access-Control-Allow-Origin", FRONT)
                .hasHeader("Access-Control-Allow-Methods", "GET,POST")
                .hasHeader("Access-Control-Allow-Headers", "authorization, content-type, idempotency-key")
                .hasHeader("Access-Control-Max-Age", "3600");
        assertThat(resposta).headers()
                .doesNotContainHeader("Access-Control-Allow-Credentials")
                .doesNotContainHeader("RateLimit");
    }

    @Test
    void preflightDeOutraOrigemERecusado() {
        MvcTestResult resposta = mvc.options().uri("/api/exportacoes")
                .header("Origin", "https://promocao-imperdivel.example")
                .header("Access-Control-Request-Method", "POST")
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(resposta).bodyText().isEqualTo("Invalid CORS request");
        assertThat(resposta).headers().doesNotContainHeader("Access-Control-Allow-Origin");
    }

    @Test
    void respostaDeVerdadeExpoeOsHeadersDeCotaAoJavaScript() {
        MvcTestResult resposta = mvc.get().uri("/api/cotacoes").header("Origin", FRONT).exchange();

        assertThat(resposta).hasStatusOk()
                .hasHeader("Access-Control-Allow-Origin", FRONT)
                .hasHeader("Access-Control-Expose-Headers", "Retry-After, RateLimit, RateLimit-Policy, Location");
    }
}
