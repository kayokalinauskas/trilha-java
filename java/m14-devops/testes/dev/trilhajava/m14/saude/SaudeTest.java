package dev.trilhajava.m14.saude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest(classes = SaudeApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("saude")
@DisplayName("Exercício 14.03 — health checks, probes e modo de manutenção")
class SaudeTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApplicationEventPublisher eventos;

    @MockitoBean
    ProvedorDePagamentos provedor;

    @BeforeEach
    void provedorNoAr() {
        when(provedor.ping()).thenReturn(Duration.ofMillis(15));
    }

    @AfterEach
    void voltaAAceitarTrafego() {
        AvailabilityChangeEvent.publish(eventos, this, ReadinessState.ACCEPTING_TRAFFIC);
    }

    MvcTestResult get(String uri) {
        return mvc.get().uri(uri).exchange();
    }

    static String corpo(MvcTestResult r) {
        try {
            return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    void manutencao(boolean ativo) {
        assertThat(mvc.post().uri("/admin/manutencao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ativo\": %s}".formatted(ativo)).exchange()).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("readiness inclui o provedor; liveness NÃO (dependência externa fora do ar não justifica reiniciar)")
    void probesComOsComponentesCertos() {
        MvcTestResult readiness = get("/actuator/health/readiness");
        assertThat(readiness).hasStatus(HttpStatus.OK);
        assertThat(JsonPath.<Map<String, Object>>read(corpo(readiness), "$.components")).containsKey("provedor");

        MvcTestResult liveness = get("/actuator/health/liveness");
        assertThat(liveness).hasStatus(HttpStatus.OK);
        assertThat(corpo(liveness)).doesNotContain("provedor");
    }

    @Test
    @DisplayName("a readiness inclui o provedor, com a latência nos detalhes")
    void readinessIncluiProvedor() {
        MvcTestResult r = get("/actuator/health/readiness");

        assertThat(JsonPath.<String>read(corpo(r), "$.components.provedor.status")).isEqualTo("UP");
        assertThat(JsonPath.<Integer>read(corpo(r), "$.components.provedor.details.latenciaMs")).isEqualTo(15);
    }

    @Test
    @DisplayName("provedor fora do ar: readiness 503 (sai do balanceador), liveness 200 (não reinicia)")
    void provedorForaDoAr() {
        when(provedor.ping()).thenThrow(new IllegalStateException("conexão recusada em 10.0.3.7:8443"));

        MvcTestResult readiness = get("/actuator/health/readiness");
        assertThat(readiness).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(JsonPath.<String>read(corpo(readiness), "$.components.provedor.status")).isEqualTo("DOWN");
        assertThat(get("/actuator/health/liveness")).hasStatus(HttpStatus.OK);
    }

    @Test
    @DisplayName("no erro, o detalhe mostra só o tipo da exceção, nunca a mensagem (pode ter IP, usuário...)")
    void naoVazaMensagem() {
        when(provedor.ping()).thenThrow(new IllegalStateException("conexão recusada em 10.0.3.7:8443"));

        String corpo = corpo(get("/actuator/health/readiness"));
        assertThat(JsonPath.<String>read(corpo, "$.components.provedor.details.erro")).isEqualTo("IllegalStateException");
        assertThat(corpo).doesNotContain("10.0.3.7");
    }

    @Test
    @DisplayName("provedor lento (acima de 1 s) conta como DOWN")
    void provedorLento() {
        when(provedor.ping()).thenReturn(Duration.ofMillis(1500));

        MvcTestResult r = get("/actuator/health/readiness");
        assertThat(r).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(JsonPath.<Integer>read(corpo(r), "$.components.provedor.details.latenciaMs")).isEqualTo(1500);
    }

    @Test
    @DisplayName("o /actuator/health geral é público: mostra só o status, sem componentes nem detalhes")
    void healthGeralSoStatus() {
        MvcTestResult r = get("/actuator/health");

        assertThat(r).hasStatus(HttpStatus.OK);
        Map<String, Object> json = JsonPath.read(corpo(r), "$");
        assertThat(json).containsEntry("status", "UP").doesNotContainKeys("components", "details");
    }

    @Test
    @DisplayName("modo de manutenção: readiness 503 com REFUSING_TRAFFIC; liveness segue 200; desligar volta ao normal")
    void modoDeManutencao() {
        manutencao(true);

        MvcTestResult readiness = get("/actuator/health/readiness");
        assertThat(readiness).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(JsonPath.<String>read(corpo(readiness), "$.components.readinessState.status")).isEqualTo("OUT_OF_SERVICE");
        assertThat(get("/actuator/health/liveness")).hasStatus(HttpStatus.OK);

        manutencao(false);
        assertThat(get("/actuator/health/readiness")).hasStatus(HttpStatus.OK);
    }

    @Test
    @DisplayName("nada além de health e prometheus: /actuator/env e /actuator/beans não existem")
    void exposicaoMinima() {
        assertThat(JsonPath.<String>read(corpo(get("/actuator/health/readiness")), "$.components.provedor.status")).isEqualTo("UP");
        assertThat(get("/actuator/env")).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(get("/actuator/beans")).hasStatus(HttpStatus.NOT_FOUND);
    }
}
