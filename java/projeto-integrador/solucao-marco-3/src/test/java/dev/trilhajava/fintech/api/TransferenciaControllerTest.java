package dev.trilhajava.fintech.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import dev.trilhajava.fintech.aplicacao.ServicoDeTransferencias;
import dev.trilhajava.fintech.aplicacao.ServicoDeTransferencias.Resultado;
import dev.trilhajava.fintech.dominio.SaldoInsuficienteException;
import dev.trilhajava.fintech.dominio.Transferencia;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Teste de FATIA: sobe só a camada web (controller, advice, Jackson, validação), com o caso de uso
 * substituído por um mock (@MockitoBean). Testa o contrato HTTP sem banco e sem regras de negócio.
 */
@WebMvcTest(TransferenciaController.class)
@DisplayName("API de transferências (fatia web)")
class TransferenciaControllerTest {

    static final UUID A = UUID.fromString("0f8b1c2e-6a4d-4f53-9a57-3c1e2b7d9a10");
    static final UUID B = UUID.fromString("7a1c2e3d-4b5f-4c6d-8e9f-0a1b2c3d4e5f");
    static final String CORPO = "{\"origem\": \"" + A + "\", \"destino\": \"" + B + "\", \"valor\": 30.00}";

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ServicoDeTransferencias servico;

    MvcTestResult post(String corpo, String chave) {
        var requisicao = mvc.post().uri("/transferencias").contentType(MediaType.APPLICATION_JSON).content(corpo);
        if (chave != null) {
            requisicao = requisicao.header("Idempotency-Key", chave);
        }
        return requisicao.exchange();
    }

    @Test
    @DisplayName("201 com Location; repetição ganha o header Idempotent-Replayed")
    void criada() {
        var t = new Transferencia(UUID.randomUUID(), A, B, new BigDecimal("30.00"), Instant.EPOCH, "k");
        given(servico.transferir(eq("k"), eq(A), eq(B), any())).willReturn(new Resultado(t, false), new Resultado(t, true));

        MvcTestResult primeira = post(CORPO, "k");
        assertThat(primeira).hasStatus(HttpStatus.CREATED);
        assertThat(primeira.getResponse().getHeader("Location")).endsWith("/transferencias/" + t.id());
        assertThat(primeira.getResponse().getHeader("Idempotent-Replayed")).isNull();
        assertThat(primeira).bodyJson().extractingPath("$.valor").isEqualTo(30.0);

        assertThat(post(CORPO, "k").getResponse().getHeader("Idempotent-Replayed")).isEqualTo("true");
    }

    @Test
    @DisplayName("sem Idempotency-Key ou com valor inválido → 400")
    void invalidas() {
        assertThat(post(CORPO, null)).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(post(CORPO.replace("30.00", "-1"), "k")).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.type").isEqualTo("https://fintech.dev/problemas/dados-invalidos");
    }

    @Test
    @DisplayName("exceção de negócio vira Problem Details: saldo insuficiente → 422")
    void saldoInsuficiente() {
        given(servico.transferir(any(), any(), any(), any()))
                .willThrow(new SaldoInsuficienteException(A, new BigDecimal("5.00")));

        MvcTestResult r = post(CORPO, "k2");
        assertThat(r).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(r).bodyJson().extractingPath("$.type").isEqualTo("https://fintech.dev/problemas/saldo-insuficiente");
        assertThat(r).bodyJson().extractingPath("$.saldoDisponivel").isEqualTo(5.0);
    }
}
