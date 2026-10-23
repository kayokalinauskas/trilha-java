package dev.trilhajava.m13.aulas.padroes;

import dev.trilhajava.m13.aulas.padroes.ResultadoDaCobranca.Aprovada;
import dev.trilhajava.m13.aulas.padroes.ResultadoDaCobranca.Recusada;

import java.util.concurrent.atomic.LongAdder;

/**
 * Solução do checkpoint: um terceiro decorator que conta desfechos. Em produção, os
 * contadores seriam métricas do Micrometer; a estrutura do decorator é a mesma.
 */
public class GatewayComContadores implements GatewayDePagamento {

    private final GatewayDePagamento original;
    private final LongAdder aprovadas = new LongAdder();
    private final LongAdder recusadas = new LongAdder();
    private final LongAdder falhas = new LongAdder();

    public GatewayComContadores(GatewayDePagamento original) {
        this.original = original;
    }

    @Override
    public ResultadoDaCobranca cobrar(SolicitacaoDeCobranca solicitacao) {
        ResultadoDaCobranca resultado;
        try {
            resultado = original.cobrar(solicitacao);
        } catch (RuntimeException e) {
            falhas.increment();
            throw e;
        }
        switch (resultado) {
            case Aprovada _ -> aprovadas.increment();
            case Recusada _ -> recusadas.increment();
        }
        return resultado;
    }

    public String resumo() {
        return "aprovadas=%d recusadas=%d falhas=%d".formatted(aprovadas.sum(), recusadas.sum(), falhas.sum());
    }
}
