package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

/**
 * O fluxo Authorization Code + PKCE de ponta a ponta, feito "à mão" como um aplicativo
 * móvel (cliente público, sem segredo) faria. O teste faz o papel do navegador: abre a
 * página de login do servidor de autorização, envia usuário e senha de teste e pega o
 * código no redirect.
 */
@DisplayName("Authorization Code + PKCE, passo a passo")
class AuthorizationCodeComPkceTest {

    static final String CLIENTE = "app-da-loja";
    static final String REDIRECT_URI = "http://127.0.0.1:8765/callback";
    static final SecureRandom ALEATORIO = new SecureRandom();

    /** O que o app guarda entre a ida ao Keycloak e a volta: nunca sai do dispositivo. */
    record Tentativa(String verifier, String state, String nonce) {
        static Tentativa nova() {
            return new Tentativa(Pkce.novoVerifier(ALEATORIO), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
    }

    /** O que volta no redirect para o app. */
    record Retorno(String code, String state) {
    }

    @Test
    @DisplayName("login, troca do código com o verifier e ID token com o nonce")
    void fluxoCompleto() {
        Tentativa tentativa = Tentativa.nova();

        Retorno retorno = loginDaAna(tentativa);
        assertThat(retorno.state()).isEqualTo(tentativa.state()); // a resposta é deste login

        HttpResponse<String> resposta = trocarCodigo(retorno.code(), tentativa.verifier());

        assertThat(resposta.statusCode()).isEqualTo(200);
        JsonNode tokens = Http.json(resposta.body());
        assertThat(tokens.has("access_token")).isTrue();
        // Cliente público não recebe refresh token aqui: quem o roubasse do aparelho renovaria
        // o acesso sem limite. Quando a sessão curta acaba, o app repete o fluxo.
        assertThat(tokens.has("refresh_token")).isFalse();
        JsonNode idToken = Http.corpoDoJwt(tokens.get("id_token").asString());
        assertThat(idToken.get("nonce").asString()).isEqualTo(tentativa.nonce()); // o ID token é desta tentativa
        assertThat(Http.textos(idToken.get("aud"))).containsExactly(CLIENTE); // é para o app, não para APIs
        assertThat(idToken.get("sub").asString()).isEqualTo("ana");
        assertThat(idToken.get("email").asString()).isEqualTo("ana@loja.test");
    }

    @Test
    @DisplayName("código interceptado não serve sem o verifier: invalid_grant")
    void verifierErrado() {
        Retorno retorno = loginDaAna(Tentativa.nova());

        HttpResponse<String> resposta = trocarCodigo(retorno.code(), Pkce.novoVerifier(ALEATORIO));

        assertThat(resposta.statusCode()).isEqualTo(400);
        JsonNode erro = Http.json(resposta.body());
        assertThat(erro.get("error").asString()).isEqualTo("invalid_grant");
    }

    @Test
    @DisplayName("o código é de uso único: a segunda troca falha com invalid_grant")
    void codigoReusado() {
        Tentativa tentativa = Tentativa.nova();
        Retorno retorno = loginDaAna(tentativa);
        assertThat(trocarCodigo(retorno.code(), tentativa.verifier()).statusCode()).isEqualTo(200);

        HttpResponse<String> segunda = trocarCodigo(retorno.code(), tentativa.verifier());

        assertThat(segunda.statusCode()).isEqualTo(400);
        assertThat(Http.json(segunda.body()).get("error").asString()).isEqualTo("invalid_grant");
    }

    @Test
    @DisplayName("redirect_uri diferente da cadastrada: o servidor recusa sem redirecionar")
    void redirectUriDiferente() {
        Navegador navegador = navegador();
        loginDaAna(Tentativa.nova(), navegador); // já logada: o servidor confere o pedido em si

        HttpResponse<String> resposta = navegador.get(urlDeAutorizacao(Tentativa.nova(), "http://127.0.0.1:8765/callback/../roubo", true));

        assertThat(resposta.statusCode()).isEqualTo(400);
        assertThat(resposta.headers().firstValue("Location")).isEmpty(); // nunca redirecionar para um endereço não cadastrado
    }

    @Test
    @DisplayName("sem code_challenge, o cliente que exige PKCE recebe invalid_request no redirect")
    void semPkce() {
        HttpResponse<String> resposta = navegador().get(urlDeAutorizacao(Tentativa.nova(), REDIRECT_URI, false));

        assertThat(resposta.statusCode()).isEqualTo(302);
        Map<String, String> parametros = parametros(resposta.headers().firstValue("Location").orElseThrow());
        assertThat(parametros).containsEntry("error", "invalid_request");
        assertThat(parametros).containsEntry("error_description", "OAuth 2.0 Parameter: code_challenge");
    }

    // ---------- o "navegador" e o "app" ----------

    private Retorno loginDaAna(Tentativa tentativa) {
        return loginDaAna(tentativa, navegador());
    }

    private Retorno loginDaAna(Tentativa tentativa, Navegador navegador) {
        // 1. O app abre o navegador na página de autorização; sem sessão, o servidor manda para o login.
        HttpResponse<String> autorizacao = navegador.get(urlDeAutorizacao(tentativa, REDIRECT_URI, true));
        assertThat(autorizacao.statusCode()).isEqualTo(302);
        String paginaDeLogin = autorizacao.headers().firstValue("Location").orElseThrow();
        assertThat(paginaDeLogin).isEqualTo(ServidorDeAutorizacaoDeTeste.emissor() + "/login");

        // 2. A pessoa digita usuário e senha NO SERVIDOR DE AUTORIZAÇÃO (o app nunca vê a senha).
        String csrf = campoCsrf(navegador.get(paginaDeLogin).body());
        HttpResponse<String> aposLogin = navegador.postFormulario(paginaDeLogin, Map.of(
                "username", "ana",
                "password", ServidorDeAutorizacaoDeTeste.SENHA_DA_ANA,
                "_csrf", csrf));
        assertThat(aposLogin.statusCode()).isEqualTo(302);

        // 3. Logada, ela volta ao pedido de autorização, que agora redireciona para o app com code e state.
        HttpResponse<String> retomada = navegador.get(aposLogin.headers().firstValue("Location").orElseThrow());
        assertThat(retomada.statusCode()).isEqualTo(302);
        String destino = retomada.headers().firstValue("Location").orElseThrow();
        assertThat(destino).startsWith(REDIRECT_URI + "?");
        Map<String, String> p = parametros(destino);
        return new Retorno(p.get("code"), p.get("state"));
    }

    /** 4. O app troca o código por tokens, provando com o verifier que foi ele quem começou o fluxo. */
    private HttpResponse<String> trocarCodigo(String code, String verifier) {
        return Http.postFormulario(ServidorDeAutorizacaoDeTeste.endpointDeToken(), Map.of(
                "grant_type", "authorization_code",
                "client_id", CLIENTE,
                "code", code,
                "redirect_uri", REDIRECT_URI,
                "code_verifier", verifier));
    }

    private static String urlDeAutorizacao(Tentativa tentativa, String redirectUri, boolean comPkce) {
        var url = UriComponentsBuilder.fromUriString(ServidorDeAutorizacaoDeTeste.emissor() + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", CLIENTE)
                .queryParam("redirect_uri", Http.codificar(redirectUri))
                .queryParam("scope", "openid%20profile%20email")
                .queryParam("state", tentativa.state())
                .queryParam("nonce", tentativa.nonce());
        if (comPkce) {
            url.queryParam("code_challenge", Pkce.challengeS256(tentativa.verifier()))
                    .queryParam("code_challenge_method", "S256");
        }
        return url.build(true).toUriString();
    }

    /** O formulário de login tem proteção contra CSRF: o token vai num campo escondido. */
    private static String campoCsrf(String html) {
        int campo = html.indexOf("name=\"_csrf\"");
        int inicio = html.indexOf("value=\"", campo) + "value=\"".length();
        return html.substring(inicio, html.indexOf('"', inicio));
    }

    private static Map<String, String> parametros(String url) {
        return UriComponentsBuilder.fromUriString(url).build().getQueryParams().toSingleValueMap()
                .entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                        e -> java.net.URLDecoder.decode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8)));
    }

    private static Navegador navegador() {
        return new Navegador(HttpClient.newBuilder()
                .cookieHandler(new CookieManager()) // o Keycloak guarda a sessão de login em cookies
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    /** Um navegador mínimo: guarda cookies e não segue redirects (o teste quer vê-los). */
    record Navegador(HttpClient cliente) {

        HttpResponse<String> get(String url) {
            // Accept: text/html diz "sou um navegador": sem ele, o servidor responde 401 em vez de mandar ao login.
            return enviar(HttpRequest.newBuilder(URI.create(url)).header("Accept", "text/html").GET().build());
        }

        HttpResponse<String> postFormulario(String url, Map<String, String> campos) {
            String corpo = campos.entrySet().stream()
                    .map(c -> Http.codificar(c.getKey()) + "=" + Http.codificar(c.getValue()))
                    .collect(java.util.stream.Collectors.joining("&"));
            return enviar(HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build());
        }

        private HttpResponse<String> enviar(HttpRequest requisicao) {
            try {
                return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                throw new IllegalStateException("falha de rede ao chamar " + requisicao.uri(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrompido ao chamar " + requisicao.uri(), e);
            }
        }
    }
}
