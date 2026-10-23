package dev.trilhajava.m06;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Solução de referência do exercício 06.03.
 *
 * Cada chamada vira um CompletableFuture no executor recebido (no teste, virtual threads).
 * orTimeout faz o futuro falhar com TimeoutException se passar do limite; exceptionally transforma
 * qualquer falha em "sem cotação" (null), e o join final nunca lança.
 */
public class CotadorDeFrete {

    private final List<Transportadora> transportadoras;
    private final Executor executor;
    private final Duration timeout;

    public CotadorDeFrete(List<Transportadora> transportadoras, Executor executor, Duration timeoutPorCotacao) {
        this.transportadoras = List.copyOf(transportadoras);
        this.executor = executor;
        this.timeout = timeoutPorCotacao;
    }

    public List<Cotacao> cotarTodas(String cepDestino, double pesoKg) {
        List<CompletableFuture<Cotacao>> futuros = transportadoras.stream()
                .map(t -> CompletableFuture
                        .supplyAsync(() -> new Cotacao(t.nome(), t.cotar(cepDestino, pesoKg)), executor)
                        .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .exceptionally(erro -> null)) // em produção: registrar o erro em log/métrica aqui
                .toList(); // toList() dispara TODAS antes de esperar qualquer uma

        return futuros.stream()
                .map(CompletableFuture::join)
                .filter(c -> c != null)
                .sorted(Comparator.comparing(Cotacao::preco))
                .toList();
    }

    public Optional<Cotacao> maisBarata(String cepDestino, double pesoKg) {
        return cotarTodas(cepDestino, pesoKg).stream().findFirst();
    }
}
