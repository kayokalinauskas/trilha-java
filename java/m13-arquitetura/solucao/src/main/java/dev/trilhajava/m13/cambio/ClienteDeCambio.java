package dev.trilhajava.m13.cambio;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente de um provedor de câmbio instável, com as defesas em camadas, de dentro para fora:
 *
 * 1. timeouts: nenhuma chamada espera para sempre;
 * 2. retry com backoff: só para falhas transitórias (5xx, timeout, conexão recusada);
 * 3. circuit breaker: com o provedor claramente fora, para de insistir por um tempo;
 * 4. fallback: o último valor conhecido, se for recente, marcado como desatualizado.
 */
public class ClienteDeCambio {

    /** Resposta do provedor: {"moeda": "USD", "valor": "5.43"}. */
    private record Resposta(String moeda, BigDecimal valor) {
    }

    /** Falha que vale a pena repetir. Interna: quem usa o cliente vê um valor ou CambioIndisponivel. */
    static class FalhaTransitoria extends RuntimeException {
        FalhaTransitoria(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }

    private final RestClient http;
    private final RetryTemplate retry;
    private final CircuitBreaker circuito;
    private final Clock relogio;
    private final Duration validade;
    private final Map<String, Cotacao> ultimas = new ConcurrentHashMap<>();

    public ClienteDeCambio(String urlBase, ConfiguracaoDoCliente config, Clock relogio) {
        this.relogio = relogio;
        this.validade = config.validadeDoUltimoValor();

        var jdk = HttpClient.newBuilder().connectTimeout(config.timeoutDeConexao()).build();
        var fabrica = new JdkClientHttpRequestFactory(jdk);
        fabrica.setReadTimeout(config.timeoutDeLeitura());
        this.http = RestClient.builder().baseUrl(urlBase).requestFactory(fabrica).build();

        // Spring Framework 7: retry nativo, sem Spring Retry. Só repete o que é transitório.
        this.retry = new RetryTemplate(RetryPolicy.builder()
                .includes(FalhaTransitoria.class)
                .maxRetries(config.maxRetries())
                .delay(config.atrasoInicial())
                .multiplier(2)
                .build());

        this.circuito = CircuitBreaker.of("cambio", CircuitBreakerConfig.custom()
                .slidingWindow(config.janelaDoCircuito(), config.janelaDoCircuito(), SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(config.percentualDeFalhas())
                .waitDurationInOpenState(config.tempoAberto())
                .permittedNumberOfCallsInHalfOpenState(1)
                .ignoreExceptions(MoedaDesconhecidaException.class)   // erro do cliente não é falha do provedor
                .clock(relogio)
                .build());
    }

    public Cotacao cotacao(String moeda) {
        try {
            // O retry fica DENTRO do circuito: uma chamada ao cliente = uma tentativa (com repetições) na janela.
            Cotacao nova = circuito.executeSupplier(() -> retry.invoke(() -> buscar(moeda)));
            ultimas.put(moeda, nova);
            return nova;
        } catch (FalhaTransitoria | CallNotPermittedException falha) {
            return ultimoValorRecente(moeda).orElseThrow(() -> new CambioIndisponivelException(moeda, falha));
        }
    }

    public boolean circuitoAberto() {
        return circuito.getState() == CircuitBreaker.State.OPEN;
    }

    private Cotacao buscar(String moeda) {
        try {
            Resposta resposta = http.get().uri("/cotacoes/{moeda}", moeda)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new MoedaDesconhecidaException(moeda);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new FalhaTransitoria("provedor respondeu " + res.getStatusCode(), null);
                    })
                    .body(Resposta.class);
            return new Cotacao(moeda, resposta.valor(), relogio.instant(), false);
        } catch (RestClientException e) {
            // Timeout de conexão ou de leitura, conexão recusada ou cortada: transitório. Antes da resposta,
            // chega como ResourceAccessException; durante a leitura do corpo, como RestClientException com
            // uma IOException lá dentro da cadeia de causas. JSON malformado e outros status não são repetidos.
            if (causadaPorIo(e)) {
                throw new FalhaTransitoria("provedor inacessível: " + e.getMessage(), e);
            }
            throw e;
        }
    }

    private static boolean causadaPorIo(Throwable erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    private Optional<Cotacao> ultimoValorRecente(String moeda) {
        return Optional.ofNullable(ultimas.get(moeda))
                .filter(c -> !c.obtidaEm().plus(validade).isBefore(relogio.instant()))
                .map(Cotacao::comoDesatualizada);
    }
}
