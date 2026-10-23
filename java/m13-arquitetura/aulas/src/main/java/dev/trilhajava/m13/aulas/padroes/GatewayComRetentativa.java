package dev.trilhajava.m13.aulas.padroes;

import java.time.Duration;

/**
 * Decorator: implementa a mesma interface do objeto que envolve e repete a chamada
 * quando ele lança {@link FalhaTemporariaException}. Só é seguro porque a solicitação
 * carrega uma chave de idempotência: repetir não cobra duas vezes.
 */
public class GatewayComRetentativa implements GatewayDePagamento {

    private final GatewayDePagamento original;
    private final int maximoDeTentativas;
    private final Duration intervalo;

    public GatewayComRetentativa(GatewayDePagamento original, int maximoDeTentativas, Duration intervalo) {
        if (maximoDeTentativas < 1) {
            throw new IllegalArgumentException("maximoDeTentativas deve ser pelo menos 1");
        }
        this.original = original;
        this.maximoDeTentativas = maximoDeTentativas;
        this.intervalo = intervalo;
    }

    @Override
    public ResultadoDaCobranca cobrar(SolicitacaoDeCobranca solicitacao) {
        for (int tentativa = 1; ; tentativa++) {
            try {
                return original.cobrar(solicitacao);
            } catch (FalhaTemporariaException e) {
                if (tentativa == maximoDeTentativas) {
                    throw e;
                }
                esperar(tentativa);
            }
        }
    }

    private void esperar(int tentativa) {
        try {
            Thread.sleep(intervalo.multipliedBy(tentativa)); // recuo linear: 1x, 2x, 3x o intervalo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrompido entre tentativas de cobrança", e);
        }
    }
}
