package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

@DisplayName("Discovery e token endpoint, em HTTP cru")
class DescobertaETokenTest {

    static final String EMISSOR = ServidorDeAutorizacaoDeTeste.emissor();

    @Test
    @DisplayName("o documento de discovery anuncia endpoints, grants e PKCE S256")
    void discovery() {
        HttpResponse<String> resposta = Http.get(EMISSOR + "/.well-known/openid-configuration");

        assertThat(resposta.statusCode()).isEqualTo(200);
        JsonNode doc = Http.json(resposta.body());
        assertThat(doc.get("issuer").asString()).isEqualTo(EMISSOR);
        assertThat(doc.get("authorization_endpoint").asString()).isEqualTo(EMISSOR + "/oauth2/authorize");
        assertThat(doc.get("token_endpoint").asString()).isEqualTo(EMISSOR + "/oauth2/token");
        assertThat(doc.get("jwks_uri").asString()).isEqualTo(EMISSOR + "/oauth2/jwks");
        assertThat(doc.get("userinfo_endpoint").asString()).isEqualTo(EMISSOR + "/userinfo");
        assertThat(doc.get("end_session_endpoint").asString()).isEqualTo(EMISSOR + "/connect/logout");
        assertThat(Http.textos(doc.get("grant_types_supported")))
                .contains("authorization_code", "client_credentials", "refresh_token");
        assertThat(Http.textos(doc.get("code_challenge_methods_supported"))).containsExactly("S256");
    }

    @Test
    @DisplayName("o JWKS publica só a chave pública que assina os tokens")
    void jwks() {
        JsonNode chave = Http.json(Http.get(EMISSOR + "/oauth2/jwks").body()).get("keys").get(0);

        assertThat(chave.get("kty").asString()).isEqualTo("RSA");
        assertThat(chave.has("kid")).isTrue();
        assertThat(chave.has("n")).isTrue();  // módulo público
        assertThat(chave.has("d")).isFalse(); // expoente privado: nunca sai do servidor
    }

    @Test
    @DisplayName("client credentials devolve um access token Bearer curto, para a API certa, sem refresh token")
    void clientCredentials() {
        HttpResponse<String> resposta = Http.postFormulario(ServidorDeAutorizacaoDeTeste.endpointDeToken(), Map.of(
                "grant_type", "client_credentials",
                "client_id", "servico-de-faturamento",
                "client_secret", ServidorDeAutorizacaoDeTeste.SEGREDO_FATURAMENTO,
                "scope", "pedidos:ler"));

        assertThat(resposta.statusCode()).isEqualTo(200);
        JsonNode corpo = Http.json(resposta.body());
        assertThat(corpo.get("token_type").asString()).isEqualTo("Bearer");
        assertThat(corpo.get("expires_in").asInt()).isBetween(290, 300); // 5 minutos
        assertThat(corpo.has("refresh_token")).isFalse(); // o serviço tem o segredo: pede outro quando vencer
        assertThat(corpo.has("id_token")).isFalse();      // não há pessoa, então não há identidade

        JsonNode claims = Http.corpoDoJwt(corpo.get("access_token").asString());
        assertThat(claims.get("iss").asString()).isEqualTo(EMISSOR);
        assertThat(claims.get("sub").asString()).isEqualTo("servico-de-faturamento");
        assertThat(Http.textos(claims.get("aud"))).containsExactly("api-pedidos");
        assertThat(Http.textos(claims.get("scope"))).containsExactly("pedidos:ler");
    }

    @Test
    @DisplayName("sem escopo de pedidos, o aud fica sendo o próprio cliente, e não a API")
    void outraAudiencia() {
        JsonNode claims = Http.corpoDoJwt(Http.tokenDeServico(
                "servico-de-relatorios", ServidorDeAutorizacaoDeTeste.SEGREDO_RELATORIOS, "relatorios:gerar"));

        assertThat(Http.textos(claims.get("aud"))).containsExactly("servico-de-relatorios");
    }

    @Test
    @DisplayName("escopo que o cliente não tem: invalid_scope")
    void escopoNaoPermitido() {
        HttpResponse<String> resposta = Http.postFormulario(ServidorDeAutorizacaoDeTeste.endpointDeToken(), Map.of(
                "grant_type", "client_credentials",
                "client_id", "servico-de-relatorios",
                "client_secret", ServidorDeAutorizacaoDeTeste.SEGREDO_RELATORIOS,
                "scope", "pedidos:ler"));

        assertThat(resposta.statusCode()).isEqualTo(400);
        assertThat(Http.json(resposta.body()).get("error").asString()).isEqualTo("invalid_scope");
    }

    @Test
    @DisplayName("segredo errado: 401 invalid_client")
    void segredoErrado() {
        HttpResponse<String> resposta = Http.postFormulario(ServidorDeAutorizacaoDeTeste.endpointDeToken(), Map.of(
                "grant_type", "client_credentials",
                "client_id", "servico-de-faturamento",
                "client_secret", "segredo-errado",
                "scope", "pedidos:ler"));

        assertThat(resposta.statusCode()).isEqualTo(401);
        assertThat(Http.json(resposta.body()).get("error").asString()).isEqualTo("invalid_client");
    }
}
