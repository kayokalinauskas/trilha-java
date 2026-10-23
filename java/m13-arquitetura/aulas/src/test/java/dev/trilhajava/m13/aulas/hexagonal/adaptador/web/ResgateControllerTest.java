package dev.trilhajava.m13.aulas.hexagonal.adaptador.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ComandoDeResgate;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ResgatarRecompensa;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Pontos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.Resgate;
import dev.trilhajava.m13.aulas.hexagonal.dominio.SaldoInsuficienteException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Teste de fatia do adaptador web: a porta de entrada é um mock; o que se testa é a tradução HTTP. */
@WebMvcTest(ResgateController.class)
class ResgateControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ResgatarRecompensa resgatarRecompensa;

    @Test
    void traduzOJsonParaOComandoEOResultadoDeVolta() {
        given(resgatarRecompensa.resgatar(new ComandoDeResgate("cli-ana", "CAFE-GRATIS")))
                .willReturn(new Resgate("r-1", "cli-ana", "CAFE-GRATIS", Pontos.de(500), Pontos.de(700),
                        Instant.parse("2026-10-06T13:00:00Z")));
        given(resgatarRecompensa.resgatar(new ComandoDeResgate("cli-bia", "CAFE-GRATIS")))
                .willThrow(new SaldoInsuficienteException("cli-bia", Pontos.de(300), Pontos.de(500)));

        assertThat(mvc.post().uri("/clientes/cli-ana/resgates")
                .contentType(MediaType.APPLICATION_JSON).content("{\"recompensa\":\"CAFE-GRATIS\"}"))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/clientes/cli-ana/resgates/r-1")
                .bodyJson().isLenientlyEqualTo("""
                        {"id":"r-1","recompensa":"CAFE-GRATIS","pontosDebitados":500,"saldoRestante":700}""");

        assertThat(mvc.post().uri("/clientes/cli-bia/resgates")
                .contentType(MediaType.APPLICATION_JSON).content("{\"recompensa\":\"CAFE-GRATIS\"}"))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.detail")
                .isEqualTo("cliente cli-bia tem 300 pontos e a recompensa custa 500");
    }
}
