package dev.trilhajava.m11.aulas.jwt;

import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.COMPARTILHADO;
import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.claimsValidos;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class AutoridadesTest {

    @DynamicPropertySource
    static void chavesDoAutorizador(DynamicPropertyRegistry registro) {
        registro.add("extrato.seguranca.jwk-set-uri", COMPARTILHADO::jwksUri);
    }

    @Autowired
    MockMvcTester mvc;

    @Test
    void tokenValidoSemOEscopoRecebe403() {
        String soLeitura = COMPARTILHADO.emitir(claimsValidos().claim("scope", "extrato:ler").build());

        MvcTestResult resposta = mvc.post().uri("/contas/minha/pix")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + soLeitura)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(resposta.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE))
                .startsWith("Bearer error=\"insufficient_scope\"");
    }

    @Test
    void escopoDePixLiberaOPix() {
        String token = COMPARTILHADO.emitir(claimsValidos().claim("scope", "extrato:ler pix:enviar").build());

        MvcTestResult resposta = mvc.post().uri("/contas/minha/pix")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();

        assertThat(resposta).hasStatusOk().bodyJson()
                .isLenientlyEqualTo("{\"cliente\": \"cliente-8f14e45f\", \"situacao\": \"EM_PROCESSAMENTO\"}");
    }

    @Test
    void papelDoClaimRolesViraRole() {
        String atendente = COMPARTILHADO.emitir(claimsValidos()
                .subject("func-0042")
                .claim("scope", "")
                .claim("roles", List.of("SUPORTE"))
                .build());

        MvcTestResult perfil = mvc.get().uri("/eu").header(HttpHeaders.AUTHORIZATION, "Bearer " + atendente).exchange();
        MvcTestResult backoffice = mvc.get().uri("/backoffice/contas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + atendente).exchange();

        assertThat(perfil).hasStatusOk().bodyJson()
                .extractingPath("$.autoridades").asArray().containsExactly("FACTOR_BEARER", "ROLE_SUPORTE");
        assertThat(backoffice).hasStatusOk();
    }

    @Test
    void clienteNaoEntraNoBackoffice() {
        String cliente = COMPARTILHADO.emitir(claimsValidos().build());

        MvcTestResult resposta = mvc.get().uri("/backoffice/contas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + cliente).exchange();

        assertThat(resposta).hasStatus(HttpStatus.FORBIDDEN);
    }
}
