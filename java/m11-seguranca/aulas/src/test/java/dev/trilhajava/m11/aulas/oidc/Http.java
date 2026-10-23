package dev.trilhajava.m11.aulas.oidc;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * HTTP "cru", sem Spring, para os testes enxergarem exatamente o que vai e volta entre
 * cliente e Keycloak. Em código de produção, quem faz isto é o Spring Security.
 */
public final class Http {

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private Http() {
    }

    public static HttpResponse<String> get(String url) {
        return enviar(HttpRequest.newBuilder(URI.create(url)).GET().build());
    }

    public static HttpResponse<String> getComToken(String url, String accessToken) {
        return enviar(HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET().build());
    }

    /** POST application/x-www-form-urlencoded, o formato do token endpoint (RFC 6749, seção 4). */
    public static HttpResponse<String> postFormulario(String url, Map<String, String> campos) {
        String corpo = campos.entrySet().stream()
                .map(c -> codificar(c.getKey()) + "=" + codificar(c.getValue()))
                .collect(Collectors.joining("&"));
        return enviar(HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build());
    }

    /** client_credentials com o segredo no corpo (client_secret_post). Devolve o access_token. */
    public static String tokenDeServico(String clientId, String segredo, String escopo) {
        HttpResponse<String> resposta = postFormulario(ServidorDeAutorizacaoDeTeste.endpointDeToken(), Map.of(
                "grant_type", "client_credentials",
                "client_id", clientId,
                "client_secret", segredo,
                "scope", escopo));
        if (resposta.statusCode() != 200) {
            throw new IllegalStateException("token endpoint respondeu " + resposta.statusCode() + ": " + resposta.body());
        }
        return json(resposta.body()).get("access_token").asString();
    }

    public static JsonNode json(String texto) {
        return JSON.readTree(texto);
    }

    /** Lê o corpo de um JWT. Só para inspecionar em teste: isto NÃO valida a assinatura. */
    public static JsonNode corpoDoJwt(String jwt) {
        String corpo = jwt.split("\\.")[1];
        return json(new String(Base64.getUrlDecoder().decode(corpo), StandardCharsets.UTF_8));
    }

    /** Um claim que pode vir como texto ou como lista, sempre como lista. */
    public static java.util.List<String> textos(JsonNode no) {
        if (no == null || no.isMissingNode() || no.isNull()) {
            return java.util.List.of();
        }
        if (no.isString()) {
            return java.util.List.of(no.asString());
        }
        var lista = new java.util.ArrayList<String>();
        no.forEach(item -> lista.add(item.asString()));
        return lista;
    }

    public static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static HttpResponse<String> enviar(HttpRequest requisicao) {
        try {
            return CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("falha de rede ao chamar " + requisicao.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrompido ao chamar " + requisicao.uri(), e);
        }
    }
}
