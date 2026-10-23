package dev.trilhajava.m11.aulas.jwt;

import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.claimsValidos;
import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Um autorizador só deste teste (outro jwk-set-uri, outro contexto): a rotação mexe no JWKS. */
@SpringBootTest
@AutoConfigureMockMvc
class RotacaoDeChavesTest {

    static final RSAKey SETEMBRO = AutorizadorDeTeste.novaChave("chave-2026-09");
    static final RSAKey OUTUBRO = AutorizadorDeTeste.novaChave("chave-2026-10");
    static final AutorizadorDeTeste AUTORIZADOR = new AutorizadorDeTeste(SETEMBRO);

    @DynamicPropertySource
    static void chavesDoAutorizador(DynamicPropertyRegistry registro) {
        registro.add("extrato.seguranca.jwk-set-uri", AUTORIZADOR::jwksUri);
    }

    @AfterAll
    static void desligar() {
        AUTORIZADOR.close();
    }

    @Autowired
    MockMvcTester mvc;

    @Test
    void trocaDeChaveSemDerrubarNinguem() {
        // Antes da rotação: o JWKS é buscado uma vez e fica em cache.
        String tokenDeSetembro = AUTORIZADOR.emitir(claimsValidos().build());
        assertThat(chamar(tokenDeSetembro)).hasStatusOk();
        assertThat(chamar(tokenDeSetembro)).hasStatusOk();
        assertThat(AUTORIZADOR.consultasAoJwks()).isEqualTo(1);

        // Fase 1: o autorizador publica a chave nova AO LADO da antiga e passa a assinar com ela.
        AUTORIZADOR.publicar(SETEMBRO, OUTUBRO);
        AUTORIZADOR.assinarCom(OUTUBRO);
        String tokenDeOutubro = AUTORIZADOR.emitir(claimsValidos().build());

        // O "kid" novo não está no cache: o decoder busca o JWKS de novo e encontra a chave.
        assertThat(chamar(tokenDeOutubro)).hasStatusOk();
        assertThat(AUTORIZADOR.consultasAoJwks()).isEqualTo(2);

        // Tokens antigos, ainda dentro do exp, continuam valendo: nenhum cliente foi derrubado.
        assertThat(chamar(tokenDeSetembro)).hasStatusOk();

        // Fase 2: a chave antiga sai do JWKS. Mas o decoder já a tem em cache e não busca de novo:
        // retirar uma chave NÃO revoga na hora os tokens assinados com ela (vale até o cache expirar).
        AUTORIZADOR.publicar(OUTUBRO);
        assertThat(chamar(tokenDeSetembro)).hasStatusOk();
        assertThat(AUTORIZADOR.consultasAoJwks()).isEqualTo(2);
    }

    private MvcTestResult chamar(String token) {
        return mvc.get().uri("/contas/minha/extrato")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();
    }
}
