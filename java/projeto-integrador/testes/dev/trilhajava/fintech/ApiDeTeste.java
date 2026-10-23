package dev.trilhajava.fintech;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cliente HTTP dos testes de aceitação: usa o HttpClient do JDK e nada da sua aplicação.
 *
 * A partir do marco 4 a API exige token. Para os testes dos marcos 1 a 3 continuarem valendo,
 * este cliente tenta obter um token de administrador em POST /auth/token na primeira chamada;
 * se essa rota não existir (404), segue sem autenticação.
 */
public final class ApiDeTeste {

    public static final String ADMIN_EMAIL = "admin@fintech.dev";
    public static final String ADMIN_SENHA = "Admin#2026-seguro";
    public static final String JWT_SEGREDO = "segredo-dos-testes-de-aceitacao-com-32-bytes-ou-mais";

    /** Uma resposta HTTP já lida. */
    public record Resposta(int status, String corpo, java.net.http.HttpHeaders headers) {

        public DocumentContext json() {
            return JsonPath.parse(corpo);
        }

        public <T> T ler(String caminho, Class<T> tipo) {
            return json().read(caminho, tipo);
        }

        public BigDecimal decimal(String caminho) {
            return new BigDecimal(json().read(caminho, Object.class).toString());
        }

        public Optional<String> header(String nome) {
            return headers.firstValue(nome);
        }

        @Override
        public String toString() {
            return status + " " + corpo;
        }
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String base;
    private volatile String token;
    private volatile boolean autenticacaoVerificada;
    private final boolean autenticarComoAdmin;

    private ApiDeTeste(int porta, String token, boolean autenticarComoAdmin) {
        this.base = "http://localhost:" + porta;
        this.token = token;
        this.autenticarComoAdmin = autenticarComoAdmin;
    }

    /** Cliente que se autentica como administrador se a API exigir (marco 4). */
    public static ApiDeTeste na(int porta) {
        return new ApiDeTeste(porta, null, true);
    }

    /** Cliente sem nenhuma credencial. */
    public static ApiDeTeste anonimo(int porta) {
        return new ApiDeTeste(porta, null, false);
    }

    /** Cliente com um token específico (marco 4). */
    public ApiDeTeste comToken(String outroToken) {
        var api = new ApiDeTeste(Integer.parseInt(base.substring(base.lastIndexOf(':') + 1)), outroToken, false);
        api.autenticacaoVerificada = true;
        return api;
    }

    public Resposta get(String caminho) {
        return enviar("GET", caminho, null, Map.of());
    }

    public Resposta post(String caminho, String json) {
        return enviar("POST", caminho, json, Map.of());
    }

    public Resposta post(String caminho, String json, Map<String, String> headers) {
        return enviar("POST", caminho, json, headers);
    }

    public Resposta put(String caminho, String json) {
        return enviar("PUT", caminho, json, Map.of());
    }

    public Resposta delete(String caminho) {
        return enviar("DELETE", caminho, null, Map.of());
    }

    public Resposta enviar(String metodo, String caminho, String json, Map<String, String> headers) {
        garantirAutenticacao();
        var requisicao = HttpRequest.newBuilder(URI.create(base + caminho))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json, application/problem+json");
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        headers.forEach(requisicao::setHeader);   // substitui o padrão (ex.: Accept)
        if (json != null) {
            requisicao.header("Content-Type", "application/json");
            requisicao.method(metodo, HttpRequest.BodyPublishers.ofString(json));
        } else {
            requisicao.method(metodo, HttpRequest.BodyPublishers.noBody());
        }
        try {
            HttpResponse<String> r = http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
            return new Resposta(r.statusCode(), r.body(), r.headers());
        } catch (IOException e) {
            throw new IllegalStateException("Falha de comunicação com a API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private void garantirAutenticacao() {
        if (autenticacaoVerificada || !autenticarComoAdmin) {
            return;
        }
        synchronized (this) {
            if (autenticacaoVerificada) {
                return;
            }
            autenticacaoVerificada = true; // evita recursão: a chamada abaixo passa por enviar()
            Resposta r = enviar("POST", "/auth/token",
                    "{\"email\": \"" + ADMIN_EMAIL + "\", \"senha\": \"" + ADMIN_SENHA + "\"}", Map.of());
            if (r.status() == 200) {
                token = r.ler("$.accessToken", String.class);
            } else if (r.status() != 404 && r.status() != 405) {
                throw new IllegalStateException("POST /auth/token existe mas não autenticou o administrador: " + r);
            }
        }
    }

    // ------------------------------------------------------------------ dados de teste

    /** CPF válido aleatório (11 dígitos, sem máscara). */
    public static String cpf() {
        var r = ThreadLocalRandom.current();
        int[] d = new int[11];
        do {
            for (int i = 0; i < 9; i++) {
                d[i] = r.nextInt(10);
            }
        } while (d[0] == d[1] && d[1] == d[2]);
        for (int n = 9; n <= 10; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += d[i] * (n + 1 - i);
            }
            int resto = soma % 11;
            d[n] = resto < 2 ? 0 : 11 - resto;
        }
        var sb = new StringBuilder();
        for (int x : d) {
            sb.append(x);
        }
        return sb.toString();
    }

    public static String novaConta(String titular, String cpf, String tipo) {
        return """
                {"titular": "%s", "cpf": "%s", "tipo": "%s"}
                """.formatted(titular, cpf, tipo);
    }

    public static String valor(String valor) {
        return "{\"valor\": " + valor + "}";
    }

    /** Abre uma conta corrente e devolve o id. */
    public String abrirConta(String titular) {
        Resposta r = post("/contas", novaConta(titular, cpf(), "CORRENTE"));
        if (r.status() != 201) {
            throw new AssertionError("Esperava 201 ao abrir conta, veio: " + r);
        }
        return r.ler("$.id", String.class);
    }

    /** Abre uma conta e deposita o valor informado. */
    public String abrirContaComSaldo(String titular, String saldo) {
        String id = abrirConta(titular);
        Resposta r = post("/contas/" + id + "/depositos", valor(saldo));
        if (r.status() != 200) {
            throw new AssertionError("Esperava 200 no depósito, veio: " + r);
        }
        return id;
    }
}
