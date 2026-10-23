package dev.trilhajava.m13.aulas.padroes.fornecedor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Um ambiente de testes do fornecedor, em memória. Falha com 503 nas primeiras chamadas
 * (quantas você pedir), recusa valores acima de R$ 10.000,00 e respeita a chave de
 * idempotência: a mesma chave devolve a mesma resposta.
 */
public class PagaFacilSandbox implements PagaFacilClient {

    private static final long LIMITE_EM_CENTAVOS = 1_000_000;

    private final int falhasAntesDeResponder;
    private final AtomicInteger chamadas = new AtomicInteger();
    private final AtomicInteger transacoes = new AtomicInteger();
    private final Map<String, PagaFacilResposta> porChave = new ConcurrentHashMap<>();

    public PagaFacilSandbox(int falhasAntesDeResponder) {
        this.falhasAntesDeResponder = falhasAntesDeResponder;
    }

    @Override
    public PagaFacilResposta createTransaction(long amountInCents, String method, String idempotencyKey) {
        if (chamadas.incrementAndGet() <= falhasAntesDeResponder) {
            throw new PagaFacilIndisponivelException(503, "upstream timeout");
        }
        return porChave.computeIfAbsent(idempotencyKey, chave -> {
            if (amountInCents > LIMITE_EM_CENTAVOS) {
                return new PagaFacilResposta("DECLINED", null, "amount_above_limit");
            }
            return new PagaFacilResposta("APPROVED", "PF-%04d".formatted(transacoes.incrementAndGet()), null);
        });
    }

    public int chamadasRecebidas() {
        return chamadas.get();
    }

    public int transacoesCriadas() {
        return transacoes.get();
    }
}
