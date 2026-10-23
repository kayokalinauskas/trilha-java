package dev.trilhajava.m11.aulas.jwt;

import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.COMPARTILHADO;
import static dev.trilhajava.m11.aulas.jwt.AutorizadorDeTeste.claimsValidos;
import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class TokensRejeitadosTest {

    @DynamicPropertySource
    static void chavesDoAutorizador(DynamicPropertyRegistry registro) {
        registro.add("extrato.seguranca.jwk-set-uri", COMPARTILHADO::jwksUri);
    }

    @Autowired
    MockMvcTester mvc;

    @Test
    void semTokenPedeAutenticacao() {
        MvcTestResult resposta = mvc.get().uri("/contas/minha/extrato").exchange();

        assertThat(resposta).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE))
                .startsWith("Bearer")
                .doesNotContain("error=");
    }

    @Test
    void tokenExpiradoHaDezMinutos() {
        Instant agora = Instant.now();
        String token = COMPARTILHADO.emitir(claimsValidos()
                .issuedAt(agora.minus(Duration.ofMinutes(15)))
                .notBefore(agora.minus(Duration.ofMinutes(15)))
                .expiresAt(agora.minus(Duration.ofMinutes(10)))
                .build());

        assertRecusado(token, "Jwt expired at");
    }

    @Test
    void expiradoHaDezSegundosAindaPassaPelaTolerancia() {
        Instant agora = Instant.now();
        String token = COMPARTILHADO.emitir(claimsValidos()
                .issuedAt(agora.minus(Duration.ofMinutes(5)))
                .notBefore(agora.minus(Duration.ofMinutes(5)))
                .expiresAt(agora.minus(Duration.ofSeconds(10)))
                .build());

        assertThat(chamar(token)).hasStatusOk();
    }

    @Test
    void tokenQueSoValeNoFuturo() {
        Instant agora = Instant.now();
        String token = COMPARTILHADO.emitir(claimsValidos()
                .notBefore(agora.plus(Duration.ofMinutes(10)))
                .expiresAt(agora.plus(Duration.ofMinutes(15)))
                .build());

        assertRecusado(token, "Jwt used before");
    }

    @Test
    void tokenEmitidoParaOutraApi() {
        String token = COMPARTILHADO.emitir(claimsValidos().audience(List.of("cartoes-api")).build());

        assertRecusado(token, "aud");
    }

    @Test
    void tokenDeOutroEmissor() {
        String token = COMPARTILHADO.emitir(claimsValidos().issuer("https://auth.banco.test/realms/funcionarios").build());

        assertRecusado(token, "iss");
    }

    @Test
    void corpoAlteradoDepoisDeAssinado() {
        String token = COMPARTILHADO.emitir(claimsValidos().build());
        String[] partes = token.split("\\.");
        String corpo = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        String corpoAlterado = corpo.replace("extrato:ler", "extrato:ler pix:enviar");
        String forjado = partes[0] + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(corpoAlterado.getBytes(StandardCharsets.UTF_8))
                + "." + partes[2];

        assertRecusado(forjado, "Invalid signature");
    }

    @Test
    void assinadoPorOutraChaveComOMesmoKid() {
        RSAKey impostora = AutorizadorDeTeste.novaChave(COMPARTILHADO.chaveDeAssinatura().getKeyID());
        String token = AutorizadorDeTeste.emitir(impostora, claimsValidos().build());

        assertRecusado(token, "Invalid signature");
    }

    @Test
    void assinadoPorChaveQueOJwksNaoConhece() {
        RSAKey desconhecida = AutorizadorDeTeste.novaChave("chave-do-atacante");
        String token = AutorizadorDeTeste.emitir(desconhecida, claimsValidos().build());

        assertRecusado(token, "no matching key");
    }

    @Test
    void tokenSemAssinaturaAlgNone() {
        Instant agora = Instant.now();
        String token = new PlainJWT(new JWTClaimsSet.Builder()
                .issuer(AutorizadorDeTeste.EMISSOR)
                .audience(AutorizadorDeTeste.AUDIENCIA)
                .subject("cliente-8f14e45f")
                .claim("scope", "extrato:ler")
                .expirationTime(Date.from(agora.plus(Duration.ofMinutes(5))))
                .build()).serialize();

        assertRecusado(token, "Unsupported algorithm of none");
    }

    @Test
    void confusaoDeAlgoritmoHs256ComAChavePublica() throws Exception {
        // O ataque: a chave PÚBLICA é conhecida por todos; o atacante a usa como segredo HMAC
        // e põe alg=HS256 no cabeçalho, torcendo para a API validar com "a chave que tem".
        RSAKey chaveDoEmissor = COMPARTILHADO.chaveDeAssinatura();
        byte[] chavePublica = chaveDoEmissor.toRSAPublicKey().getEncoded();
        Instant agora = Instant.now();
        var forjado = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(chaveDoEmissor.getKeyID()).build(),
                new JWTClaimsSet.Builder()
                        .issuer(AutorizadorDeTeste.EMISSOR)
                        .audience(AutorizadorDeTeste.AUDIENCIA)
                        .subject("cliente-8f14e45f")
                        .claim("scope", "extrato:ler pix:enviar")
                        .expirationTime(Date.from(agora.plus(Duration.ofMinutes(5))))
                        .build());
        forjado.sign(new MACSigner(chavePublica));

        assertRecusado(forjado.serialize(), "Another algorithm expected");
    }

    @Test
    void tipoDeTokenInesperado() {
        String token = AutorizadorDeTeste.emitir(COMPARTILHADO.chaveDeAssinatura(),
                JwsHeader.with(SignatureAlgorithm.RS256)
                        .keyId(COMPARTILHADO.chaveDeAssinatura().getKeyID())
                        .type("at+jwt")
                        .build(),
                claimsValidos().build());

        assertRecusado(token, "typ");
    }

    private MvcTestResult chamar(String token) {
        return mvc.get().uri("/contas/minha/extrato")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();
    }

    private void assertRecusado(String token, String trechoDoErro) {
        MvcTestResult resposta = chamar(token);
        assertThat(resposta).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE))
                .startsWith("Bearer error=\"invalid_token\"")
                .contains(trechoDoErro);
    }
}
