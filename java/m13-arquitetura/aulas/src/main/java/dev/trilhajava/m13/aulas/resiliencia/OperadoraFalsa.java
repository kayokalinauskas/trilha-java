package dev.trilhajava.m13.aulas.resiliencia;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Uma operadora de convênio de mentira, com HTTP de verdade (o servidor embutido no JDK).
 * Serve para provocar, sem Docker e sem rede externa, as falhas que a aula estuda:
 * lentidão, 503 durante um deploy e indisponibilidade prolongada.
 *
 * <pre>
 * GET  /elegibilidade/{carteirinha}  200 {"carteirinha": "...", "plano": "...", "ativa": true}
 *                                    404 carteirinha desconhecida (qualquer uma que comece com "000")
 * POST /autorizacoes                 201 {"numero": "AUT-1"}; o cabeçalho Idempotency-Key é obrigatório
 * </pre>
 */
public final class OperadoraFalsa implements AutoCloseable {

    private final HttpServer servidor;
    private final AtomicInteger chamadas = new AtomicInteger();
    private final AtomicInteger falhasProgramadas = new AtomicInteger();
    private final AtomicInteger numeroDaAutorizacao = new AtomicInteger();
    private final Map<String, String> autorizacoesPorChave = new ConcurrentHashMap<>();
    private final AtomicInteger respostasLentas = new AtomicInteger();
    private volatile Duration atraso = Duration.ZERO;
    private volatile Duration atrasoTemporario = Duration.ZERO;
    private volatile boolean fora;

    private OperadoraFalsa(HttpServer servidor) {
        this.servidor = servidor;
    }

    /** Sobe a operadora numa porta livre. Cada requisição roda numa thread virtual. */
    public static OperadoraFalsa iniciar() {
        try {
            HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            var operadora = new OperadoraFalsa(servidor);
            servidor.createContext("/", operadora::atender);
            servidor.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            servidor.start();
            return operadora;
        } catch (IOException e) {
            throw new IllegalStateException("não foi possível subir a operadora falsa", e);
        }
    }

    public String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    /** Toda resposta passa a demorar este tempo (o processamento acontece ANTES da espera). */
    public OperadoraFalsa comAtraso(Duration atraso) {
        this.atraso = atraso;
        return this;
    }

    /** Só as próximas {@code quantidade} respostas demoram {@code atraso}; as seguintes voltam ao normal. */
    public OperadoraFalsa comAtrasoNasProximas(int quantidade, Duration atraso) {
        this.atrasoTemporario = atraso;
        respostasLentas.set(quantidade);
        return this;
    }

    /** As próximas {@code quantidade} requisições recebem 503, como num deploy. */
    public OperadoraFalsa falharAsProximas(int quantidade) {
        falhasProgramadas.set(quantidade);
        return this;
    }

    /** Fora do ar por tempo indeterminado: tudo recebe 503. */
    public OperadoraFalsa fora(boolean fora) {
        this.fora = fora;
        return this;
    }

    public int chamadasRecebidas() {
        return chamadas.get();
    }

    public int autorizacoesCriadas() {
        return autorizacoesPorChave.size();
    }

    private void atender(HttpExchange troca) throws IOException {
        try (troca) {
            chamadas.incrementAndGet();
            if (fora || falhasProgramadas.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
                responder(troca, 503, "{\"erro\": \"indisponível\"}");
                return;
            }
            String caminho = troca.getRequestURI().getPath();
            String metodo = troca.getRequestMethod();
            if (metodo.equals("GET") && caminho.startsWith("/elegibilidade/")) {
                String carteirinha = caminho.substring("/elegibilidade/".length());
                esperar();
                if (carteirinha.startsWith("000")) {
                    responder(troca, 404, "{\"erro\": \"carteirinha desconhecida\"}");
                } else {
                    responder(troca, 200, """
                            {"carteirinha": "%s", "plano": "Enfermaria Nacional", "ativa": true}"""
                            .formatted(carteirinha));
                }
            } else if (metodo.equals("POST") && caminho.equals("/autorizacoes")) {
                String chave = troca.getRequestHeaders().getFirst("Idempotency-Key");
                if (chave == null) {
                    responder(troca, 400, "{\"erro\": \"Idempotency-Key obrigatório\"}");
                    return;
                }
                troca.getRequestBody().readAllBytes();
                // Processa primeiro (a autorização passa a existir) e só depois demora a responder:
                // é o cenário em que o cliente vê timeout, mas o efeito já aconteceu.
                String numero = autorizacoesPorChave.computeIfAbsent(chave,
                        k -> "AUT-" + numeroDaAutorizacao.incrementAndGet());
                esperar();
                responder(troca, 201, "{\"numero\": \"" + numero + "\"}");
            } else {
                responder(troca, 404, "{\"erro\": \"rota desconhecida\"}");
            }
        }
    }

    private void esperar() {
        try {
            boolean lenta = respostasLentas.getAndUpdate(n -> Math.max(0, n - 1)) > 0;
            Thread.sleep(lenta ? atrasoTemporario : atraso);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void responder(HttpExchange troca, int status, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json");
        try {
            troca.sendResponseHeaders(status, bytes.length);
            troca.getResponseBody().write(bytes);
        } catch (IOException e) {
            // O cliente desistiu (timeout de leitura) e fechou a conexão: não há para quem responder.
            System.getLogger(OperadoraFalsa.class.getName())
                    .log(System.Logger.Level.DEBUG, "cliente encerrou a conexão antes da resposta", e);
        }
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
