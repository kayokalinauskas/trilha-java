package dev.trilhajava.m08.cambio;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * Um servidor HTTP de verdade (do próprio JDK), em uma porta aleatória, que finge ser a API de
 * cotações. Cada teste programa as respostas por moeda e depois confere as requisições recebidas.
 */
final class ApiDeCotacoesFalsa {

    record Resposta(int status, String corpo, long atrasoMs) {
    }

    private final HttpServer servidor;
    private final Map<String, Resposta> respostas = new ConcurrentHashMap<>();
    private final List<String> requisicoes = new CopyOnWriteArrayList<>();

    ApiDeCotacoesFalsa() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        servidor.setExecutor(Executors.newCachedThreadPool());
        servidor.createContext("/", this::responder);
        servidor.start();
    }

    String url() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    void cotacao(String moeda, String compra, String venda) {
        respostas.put(moeda, new Resposta(200, """
                {"moeda": "%s", "compra": %s, "venda": %s, "dataHora": "2026-09-28T13:00:00Z"}
                """.formatted(moeda, compra, venda), 0));
    }

    void falha(String moeda, int status) {
        respostas.put(moeda, new Resposta(status, "{\"erro\": \"indisponivel\"}", 0));
    }

    void lenta(String moeda, long atrasoMs) {
        respostas.put(moeda, new Resposta(200, "{\"moeda\": \"" + moeda + "\", \"compra\": 1, \"venda\": 1, \"dataHora\": \"2026-09-28T13:00:00Z\"}", atrasoMs));
    }

    List<String> requisicoes() {
        return List.copyOf(requisicoes);
    }

    void limpar() {
        respostas.clear();
        requisicoes.clear();
    }

    private void responder(HttpExchange troca) throws IOException {
        String caminho = troca.getRequestURI().getPath();
        requisicoes.add(troca.getRequestMethod() + " " + caminho);
        String moeda = caminho.substring(caminho.lastIndexOf('/') + 1);
        Resposta resposta = respostas.getOrDefault(moeda, new Resposta(404, "{\"erro\": \"moeda desconhecida\"}", 0));
        if (resposta.atrasoMs() > 0) {
            try {
                Thread.sleep(resposta.atrasoMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        byte[] corpo = resposta.corpo().getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().add("Content-Type", "application/json");
        try {
            troca.sendResponseHeaders(resposta.status(), corpo.length);
            troca.getResponseBody().write(corpo);
        } catch (IOException clienteDesistiu) {
            // o cliente deu timeout e fechou a conexão: esperado no teste de timeout
        } finally {
            troca.close();
        }
    }
}
