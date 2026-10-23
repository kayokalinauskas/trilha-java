package dev.trilhajava.m13.aulas.resiliencia;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;

/**
 * Confere o convênio de um paciente com as defesas em camadas, de dentro para fora:
 * timeout (no cliente) → retry com backoff e jitter → circuit breaker → fallback honesto.
 */
public class VerificadorDeElegibilidade {

    private static final Logger log = LoggerFactory.getLogger(VerificadorDeElegibilidade.class);

    private final ClienteDaOperadora operadora;
    private final RetryTemplate retry;
    private final CircuitBreaker circuito;

    public VerificadorDeElegibilidade(ClienteDaOperadora operadora, PoliticaDeResiliencia politica) {
        this.operadora = operadora;

        this.retry = new RetryTemplate(RetryPolicy.builder()
                .includes(OperadoraIndisponivelException.class)   // só o transitório; 404 sobe na hora
                .maxRetries(politica.repeticoes())                 // 1 tentativa + 2 repetições
                .delay(politica.esperaInicial())                   // 200 ms, 400 ms...
                .multiplier(2)
                .jitter(politica.jitter())                         // ± aleatório: espalha as repetições
                .timeout(politica.orcamentoTotal())                // nunca passa do orçamento
                .build());

        this.circuito = CircuitBreaker.of("operadora", CircuitBreakerConfig.custom()
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(politica.janelaDoCircuito())    // olha as últimas N chamadas
                .minimumNumberOfCalls(politica.minimoDeChamadas()) // sem amostra mínima, não decide
                .failureRateThreshold(politica.percentualDeFalhas())
                .waitDurationInOpenState(politica.tempoAberto())
                .permittedNumberOfCallsInHalfOpenState(2)
                .ignoreExceptions(CarteirinhaDesconhecidaException.class) // erro de quem chama
                .build());

        // Toda mudança de estado vira log (e, em produção, métrica e alerta).
        circuito.getEventPublisher().onStateTransition(evento ->
                log.warn("circuito '{}': {}", evento.getCircuitBreakerName(), evento.getStateTransition()));
    }

    public Elegibilidade verificar(String carteirinha) {
        try {
            RespostaDaOperadora resposta = circuito.executeSupplier(
                    () -> retry.invoke(() -> operadora.consultarElegibilidade(carteirinha)));
            return Elegibilidade.daOperadora(resposta);
        } catch (CallNotPermittedException e) {
            log.info("circuito aberto; elegibilidade de {} não confirmada", carteirinha);
            return Elegibilidade.naoConfirmada(carteirinha, "operadora em falha; circuito aberto");
        } catch (OperadoraIndisponivelException e) {
            log.info("operadora indisponível depois das repetições: {}", e.getMessage());
            return Elegibilidade.naoConfirmada(carteirinha, "operadora indisponível");
        }
        // CarteirinhaDesconhecidaException não é capturada: é resposta, não falha.
    }

    public CircuitBreaker.State estadoDoCircuito() {
        return circuito.getState();
    }
}
