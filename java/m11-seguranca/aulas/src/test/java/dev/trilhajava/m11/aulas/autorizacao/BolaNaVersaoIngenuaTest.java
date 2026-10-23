package dev.trilhajava.m11.aulas.autorizacao;

import static dev.trilhajava.m11.aulas.autorizacao.Tokens.como;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * O EXPLOIT documentado em teste: estes testes passam porque a versão /v0 ESTÁ vulnerável.
 * Cada um descreve o que um atacante autenticado consegue fazer.
 */
@SpringBootTest(properties = Tokens.SEGREDO)
@AutoConfigureMockMvc
@DirtiesContext // o POST grava no repositório em memória
@DisplayName("v0 (vulnerável)")
class BolaNaVersaoIngenuaTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    RepositorioDeProntuarios repositorio;

    @Test
    @DisplayName("BOLA: o paciente Bruno lê o prontuário da Ana trocando o id")
    void brunoLeOProntuarioDaAna() {
        var resposta = mvc.get().uri("/v0/prontuarios/1001").with(como("pac-bruno")).exchange();

        assertThat(resposta).hasStatusOk();
        assertThat(resposta).bodyJson().extractingPath("$.anotacoes")
                .isEqualTo("Suspeita de enxaqueca; pedir ressonância.");
    }

    @Test
    @DisplayName("BOLA entre tenants: um médico da clínica Lua lê um prontuário da clínica Sol")
    void medicoDeOutraClinicaLe() {
        var resposta = mvc.get().uri("/v0/prontuarios/1002").with(como("med-heitor")).exchange();

        assertThat(resposta).hasStatusOk();
        assertThat(resposta).bodyJson().extractingPath("$.clinicaId").isEqualTo("sol");
    }

    @Test
    @DisplayName("mass assignment: a recepção da Lua cria um prontuário DENTRO da Sol, já com anotações")
    void massAssignment() {
        var resposta = mvc.post().uri("/v0/prontuarios").with(como("atd-iara"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"id": 1002, "clinicaId": "sol", "pacienteId": "pac-bruno", "medicoId": "med-diego",
                         "queixa": "Check-up anual", "anotacoes": "Paciente saudável; suspender estatina."}""")
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        // pior ainda: o id 1002 já existia, e o prontuário verdadeiro do Bruno foi SOBRESCRITO
        assertThat(repositorio.porId(1002).orElseThrow().anotacoes())
                .isEqualTo("Paciente saudável; suspender estatina.");
    }
}
