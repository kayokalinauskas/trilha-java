package dev.trilhajava.m11.aulas.jwt;

import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.COMPARTILHADO;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class TokenValidoTest {

    @DynamicPropertySource
    static void chavesDoAutorizador(DynamicPropertyRegistry registro) {
        registro.add("extrato.seguranca.jwk-set-uri", COMPARTILHADO::jwksUri);
    }

    @Autowired
    MockMvcTester mvc;

    @Test
    void tokenValidoChegaAoController() {
        String token = COMPARTILHADO.emitir(AutorizadorDeTeste.claimsValidos()
                .claim("scope", "extrato:ler pix:enviar")
                .build());

        MvcTestResult resposta = mvc.get().uri("/eu")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();

        assertThat(resposta).hasStatusOk().bodyJson().isLenientlyEqualTo("""
                {
                  "cliente": "cliente-8f14e45f",
                  "clienteApp": "app-mobile",
                  "autoridades": ["FACTOR_BEARER", "SCOPE_extrato:ler", "SCOPE_pix:enviar"]
                }""");
    }

    @Test
    void escopoCertoAbreOExtrato() {
        String token = COMPARTILHADO.emitir(AutorizadorDeTeste.claimsValidos().build());

        MvcTestResult resposta = mvc.get().uri("/contas/minha/extrato")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();

        assertThat(resposta).hasStatusOk().bodyJson()
                .isLenientlyEqualTo("{\"cliente\": \"cliente-8f14e45f\", \"saldo\": 1530.42}");
    }
}
