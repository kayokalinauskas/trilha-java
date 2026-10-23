package dev.trilhajava.m11.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Confere a solução de referência do exercício 11.02 contra os exemplos do enunciado (garante que o que o site
 * mostra funciona). Não faz parte do starter.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Exercício 11.02 — API stateless com JWT e papéis")
class DeliveryApiTest {

    static final String PROBLEMAS = "https://api.delivery.dev/problemas/";

    @Autowired
    MockMvcTester mvc;

    @Value("${delivery.jwt.segredo}")
    String segredo;

    // ------------------------------------------------------------------ auxiliares

    MvcTestResult login(String usuario, String senha) {
        return mvc.post().uri("/auth/token").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\": \"" + usuario + "\", \"senha\": \"" + senha + "\"}").exchange();
    }

    String tokenDe(String usuario, String senha) {
        MvcTestResult r = login(usuario, senha);
        assertThat(r).as("login de " + usuario).hasStatusOk();
        return json(r).read("$.accessToken", String.class);
    }

    static DocumentContext json(MvcTestResult r) {
        return JsonPath.parse(new String(r.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    MvcTestResult get(String uri, String token) {
        var requisicao = mvc.get().uri(uri);
        return (token == null ? requisicao : requisicao.header("Authorization", "Bearer " + token)).exchange();
    }

    MvcTestResult post(String uri, String token) {
        return mvc.post().uri(uri).header("Authorization", "Bearer " + token).exchange();
    }

    /** Forja um token HS256 como um atacante (ou um teste) faria. */
    static String forjar(String segredo, String emissor, String usuario, List<String> papeis, Instant expira) {
        try {
            var claims = new JWTClaimsSet.Builder().issuer(emissor).subject(usuario).claim("roles", papeis)
                    .issueTime(Date.from(Instant.now().minusSeconds(60))).expirationTime(Date.from(expira)).build();
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(segredo.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static void problema(MvcTestResult r, HttpStatus status, String tipo) {
        assertThat(r).hasStatus(status).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(json(r).read("$.type", String.class)).isEqualTo(PROBLEMAS + tipo);
    }

    // ------------------------------------------------------------------ testes

    @Test
    @DisplayName("rotas públicas funcionam sem token")
    void publica() {
        assertThat(get("/publico/status", null)).hasStatusOk();
    }

    @Nested
    @DisplayName("POST /auth/token")
    class Login {

        @Test
        @DisplayName("emite um JWT HS256 assinado, com emissor, usuário, papéis e validade de 15 minutos")
        void emite() throws Exception {
            MvcTestResult r = login("ana", "cafe-com-leite-123");
            assertThat(r).hasStatusOk();
            assertThat(json(r).read("$.tokenType", String.class)).isEqualTo("Bearer");
            assertThat(json(r).read("$.expiresIn", Integer.class)).isEqualTo(900);

            SignedJWT jwt = SignedJWT.parse(json(r).read("$.accessToken", String.class));
            assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
            assertThat(jwt.verify(new MACVerifier(segredo.getBytes(StandardCharsets.UTF_8)))).as("assinatura").isTrue();
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            assertThat(claims.getIssuer()).isEqualTo("delivery-api");
            assertThat(claims.getSubject()).isEqualTo("ana");
            assertThat(claims.getStringListClaim("roles")).containsExactly("CLIENTE");
            assertThat(Duration.between(claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant()))
                    .isEqualTo(Duration.ofMinutes(15));
        }

        @Test
        @DisplayName("senha errada e usuário inexistente → a MESMA resposta 401 credenciais-invalidas")
        void invalidas() {
            MvcTestResult senhaErrada = login("ana", "senha-errada");
            MvcTestResult inexistente = login("zezinho", "qualquer");
            problema(senhaErrada, HttpStatus.UNAUTHORIZED, "credenciais-invalidas");
            problema(inexistente, HttpStatus.UNAUTHORIZED, "credenciais-invalidas");
            assertThat(json(senhaErrada).read("$.detail", String.class)).isEqualTo(json(inexistente).read("$.detail", String.class));
        }

        @Test
        @DisplayName("corpo sem usuário ou senha → 400 em Problem Details (validação), não 401")
        void corpoInvalido() {
            MvcTestResult r = mvc.post().uri("/auth/token").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"usuario\": \"\"}").exchange();
            assertThat(r).hasStatus(HttpStatus.BAD_REQUEST).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        }
    }

    @Nested
    @DisplayName("autenticação")
    class Autenticacao {

        @Test
        @DisplayName("sem token → 401 com WWW-Authenticate: Bearer e Problem Details")
        void semToken() {
            MvcTestResult r = get("/pedidos/meus", null);
            problema(r, HttpStatus.UNAUTHORIZED, "nao-autenticado");
            assertThat(r.getResponse().getHeader("WWW-Authenticate")).startsWith("Bearer");
        }

        @Test
        @DisplayName("com o token do login, o usuário vem do token")
        void comToken() {
            MvcTestResult r = get("/pedidos/meus", tokenDe("ana", "cafe-com-leite-123"));
            assertThat(r).hasStatusOk();
            assertThat(json(r).read("$.usuario", String.class)).isEqualTo("ana");
            assertThat(json(r).read("$.pedidos", List.class)).containsExactly("PED-1001", "PED-1002");
        }

        @Test
        @DisplayName("token assinado com OUTRO segredo → 401")
        void outroSegredo() {
            String falso = forjar("um-segredo-diferente-com-mais-de-32-bytes!!", "delivery-api", "carla",
                    List.of("ADMIN"), Instant.now().plusSeconds(600));
            problema(get("/admin/relatorio", falso), HttpStatus.UNAUTHORIZED, "nao-autenticado");
        }

        @Test
        @DisplayName("token expirado → 401")
        void expirado() {
            String velho = forjar(segredo, "delivery-api", "ana", List.of("CLIENTE"), Instant.now().minusSeconds(120));
            problema(get("/pedidos/meus", velho), HttpStatus.UNAUTHORIZED, "nao-autenticado");
        }

        @Test
        @DisplayName("token de OUTRO emissor (mesmo segredo) → 401")
        void outroEmissor() {
            String estrangeiro = forjar(segredo, "outra-api", "ana", List.of("CLIENTE"), Instant.now().plusSeconds(600));
            problema(get("/pedidos/meus", estrangeiro), HttpStatus.UNAUTHORIZED, "nao-autenticado");
        }

        @Test
        @DisplayName("token sem assinatura (alg: none) → 401")
        void semAssinatura() {
            var claims = new JWTClaimsSet.Builder().issuer("delivery-api").subject("carla").claim("roles", List.of("ADMIN"))
                    .expirationTime(Date.from(Instant.now().plusSeconds(600))).build();
            problema(get("/admin/relatorio", new PlainJWT(claims).serialize()), HttpStatus.UNAUTHORIZED, "nao-autenticado");
        }

        @Test
        @DisplayName("a API é stateless: nenhuma sessão ou cookie é criado")
        void stateless() {
            MvcTestResult r = get("/pedidos/meus", tokenDe("ana", "cafe-com-leite-123"));
            assertThat(r.getResponse().getHeader("Set-Cookie")).isNull();
            assertThat(r.getRequest().getSession(false)).isNull();
        }
    }

    @Nested
    @DisplayName("autorização por papel")
    class Autorizacao {

        @Test
        @DisplayName("CLIENTE não conclui entregas (403 acesso-negado); ENTREGADOR conclui")
        void entregas() {
            problema(post("/entregas/7/concluir", tokenDe("ana", "cafe-com-leite-123")), HttpStatus.FORBIDDEN, "acesso-negado");
            MvcTestResult r = post("/entregas/7/concluir", tokenDe("bruno", "moto-rapida-456"));
            assertThat(r).hasStatusOk();
            assertThat(json(r).read("$.concluidaPor", String.class)).isEqualTo("bruno");
        }

        @Test
        @DisplayName("só ADMIN acessa /admin")
        void admin() {
            problema(get("/admin/relatorio", tokenDe("bruno", "moto-rapida-456")), HttpStatus.FORBIDDEN, "acesso-negado");
            assertThat(get("/admin/relatorio", tokenDe("carla", "painel-admin-789"))).hasStatusOk();
        }

        @Test
        @DisplayName("a API confia na ASSINATURA: um token válido assinado com o segredo vale — proteja o segredo")
        void confiaNaAssinatura() {
            String forjadoComOSegredo = forjar(segredo, "delivery-api", "alguem", List.of("ADMIN"), Instant.now().plusSeconds(600));
            assertThat(get("/admin/relatorio", forjadoComOSegredo)).hasStatusOk();
        }
    }
}
