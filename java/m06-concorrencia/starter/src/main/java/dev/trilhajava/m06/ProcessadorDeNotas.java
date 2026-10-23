package dev.trilhajava.m06;

import java.util.List;
import java.util.function.Function;

/**
 * Exercício 06.04 — Emite notas fiscais em lote chamando um serviço externo lento.
 * Rode: mvn test -Dtest=ProcessadorDeNotasTest
 *
 * Use virtual threads (uma por nota) e limite quantas chamadas acontecem AO MESMO TEMPO:
 * o serviço externo aceita no máximo maxSimultaneas conexões.
 */
public class ProcessadorDeNotas {

    public ResultadoLote processar(List<String> idsPedidos, Function<String, String> emitirNota, int maxSimultaneas) {
        throw new UnsupportedOperationException("TODO: Executors.newVirtualThreadPerTaskExecutor + Semaphore");
    }
}
