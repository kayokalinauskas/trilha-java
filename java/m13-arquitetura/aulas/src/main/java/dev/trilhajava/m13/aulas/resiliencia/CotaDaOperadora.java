package dev.trilhajava.m13.aulas.resiliencia;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Rate limiter do lado do cliente: o contrato com a operadora permite N requisições por segundo.
 * Passar disso rende 429 e, em alguns contratos, bloqueio. Melhor gastar a cota com consciência.
 */
public class CotaDaOperadora {

    private final RateLimiter limitador;

    public CotaDaOperadora(int requisicoesPorSegundo, Duration esperaMaximaPorVaga) {
        this.limitador = RateLimiter.of("cota-da-operadora", RateLimiterConfig.custom()
                .limitForPeriod(requisicoesPorSegundo)          // quantas vagas por período
                .limitRefreshPeriod(Duration.ofSeconds(1))      // o período
                .timeoutDuration(esperaMaximaPorVaga)           // quanto esperar por uma vaga; ZERO = recusa na hora
                .build());
    }

    /** Executa a chamada se houver vaga; senão lança {@link RequestNotPermitted}. */
    public <T> T executar(Supplier<T> chamada) {
        return limitador.executeSupplier(chamada);
    }

    /**
     * Demonstração: rajada de 8 chamadas com cota de 5 por segundo, e outra rajada 1 s depois.
     * <pre>mvn -q compile exec:java -Dexec.mainClass=dev.trilhajava.m13.aulas.resiliencia.CotaDaOperadora</pre>
     */
    public static void main(String[] args) throws InterruptedException {
        var cota = new CotaDaOperadora(5, Duration.ZERO);
        for (int rajada = 1; rajada <= 2; rajada++) {
            var linha = new StringBuilder("rajada " + rajada + ":");
            for (int i = 1; i <= 8; i++) {
                try {
                    cota.executar(() -> "ok");
                    linha.append(" passou");
                } catch (RequestNotPermitted e) {
                    linha.append(" RECUSADA");
                }
            }
            System.out.println(linha);
            Thread.sleep(1_000);
        }
    }
}
