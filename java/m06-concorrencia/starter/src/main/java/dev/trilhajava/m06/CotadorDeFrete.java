package dev.trilhajava.m06;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * Exercício 06.03 — Cotação de frete em paralelo.
 * Rode: mvn test -Dtest=CotadorDeFreteTest
 */
public class CotadorDeFrete {

    public CotadorDeFrete(List<Transportadora> transportadoras, Executor executor, Duration timeoutPorCotacao) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Cotações que responderam dentro do timeout, ordenadas por preço (menor primeiro). */
    public List<Cotacao> cotarTodas(String cepDestino, double pesoKg) {
        throw new UnsupportedOperationException("TODO: CompletableFuture.supplyAsync + orTimeout + exceptionally");
    }

    public Optional<Cotacao> maisBarata(String cepDestino, double pesoKg) {
        throw new UnsupportedOperationException("TODO");
    }
}
