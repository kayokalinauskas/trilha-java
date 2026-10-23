package dev.trilhajava.m11.aulas.filtros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class RespostasDeErroTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void semCredencialDa401EmProblemDetailsComODesafioBasic() {
        MvcTestResult resposta = mvc.get().uri("/api/consultas")
                .header(FiltroDeCorrelacao.HEADER, "req-401")
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .hasHeader("WWW-Authenticate", "Basic realm=\"clinica\"")
                .hasHeader(FiltroDeCorrelacao.HEADER, "req-401")
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "type": "https://clinica.exemplo.dev/problemas/nao-autenticado",
                          "title": "Não autenticado",
                          "status": 401,
                          "instance": "/api/consultas",
                          "correlationId": "req-401"
                        }""");
    }

    @Test
    void senhaErradaTambemDa401() {
        MvcTestResult resposta = mvc.get().uri("/api/consultas")
                .with(httpBasic("recepcao", "senha-errada"))
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson().extractingPath("$.title").isEqualTo("Não autenticado");
    }

    @Test
    void perfilSemPermissaoDa403EmProblemDetailsSemDesafio() {
        MvcTestResult resposta = mvc.get().uri("/api/consultas")
                .with(httpBasic("admin", "admin-123"))
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.FORBIDDEN)
                .doesNotContainHeader("WWW-Authenticate")
                .bodyJson().isLenientlyEqualTo("""
                        {"type": "https://clinica.exemplo.dev/problemas/acesso-negado", "status": 403}""");
    }

    @Test
    void aMethodSecurityTambemSaiComoProblemDetails() {
        MvcTestResult resposta = mvc.get().uri("/api/consultas/1/prontuario")
                .with(httpBasic("recepcao", "recepcao-123"))
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.FORBIDDEN)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void aApiEStatelessENaoCriaSessao() {
        MvcTestResult resposta = mvc.get().uri("/api/consultas")
                .with(httpBasic("recepcao", "recepcao-123"))
                .exchange();

        assertThat(resposta).hasStatusOk().doesNotContainHeader("Set-Cookie");
        assertThat(resposta.getRequest().getSession(false)).isNull();
    }

    @Test
    void oConsoleRedirecionaQuemNaoEstaLogadoParaOLogin() {
        assertThat(mvc.get().uri("/admin/painel"))
                .hasStatus(HttpStatus.FOUND)
                .hasRedirectedUrl("/login");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void oConsoleExigeTokenCsrfEmPost() {
        assertThat(mvc.post().uri("/admin/cache/limpar")).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(mvc.post().uri("/admin/cache/limpar").with(csrf())).hasStatusOk();
    }
}
