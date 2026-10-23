package dev.trilhajava.m08.cambio;

import java.math.BigDecimal;

/**
 * Exercício 08.04 — converta reais para outra moeda usando uma API HTTP de cotações.
 *
 * Você vai criar (neste pacote):
 *   CambioProperties  → @ConfigurationProperties("cambio"): base-url, timeout (2s), validade (60s),
 *                        validade-maxima-fallback (24h)
 *   CotacaoClient     → HTTP interface (@HttpExchange/@GetExchange) para GET /cotacoes/{moeda}
 *   CambioConfig      → @Bean do CotacaoClient (RestClient com timeout) e @Bean de java.time.Clock
 *
 * E implementar esta classe como um @Service que recebe CotacaoClient, CambioProperties e Clock.
 */
public class ConversorDeMoedas {

    public Conversao converter(BigDecimal valorEmReais, String moeda) {
        throw new UnsupportedOperationException("TODO");
    }
}
