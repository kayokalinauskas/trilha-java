package dev.trilhajava.m13.aulas.resiliencia;

import org.springframework.resilience.annotation.Retryable;

/**
 * Pede à operadora a autorização de um procedimento (um POST que cria algo do outro lado).
 * O retry nativo do Spring Framework 7 só é seguro aqui por causa da chave de idempotência.
 * É um bean (veja ConfiguracaoDeResiliencia): a anotação só funciona quando a chamada passa pelo proxy.
 */
public class AutorizacaoDeProcedimentos {

    private final ClienteDaOperadora operadora;

    public AutorizacaoDeProcedimentos(ClienteDaOperadora operadora) {
        this.operadora = operadora;
    }

    /**
     * Quem chama gera a chave UMA vez (por exemplo, ao receber o pedido do médico) e a passa aqui.
     * Cada repetição reenvia a MESMA chave; a operadora devolve a autorização já criada em vez de criar outra.
     */
    @Retryable(includes = OperadoraIndisponivelException.class,
            maxRetries = 2, delay = 50, multiplier = 2, jitter = 10)
    public String solicitar(String carteirinha, String procedimento, String chaveDeIdempotencia) {
        return operadora.solicitarAutorizacao(carteirinha, procedimento, chaveDeIdempotencia);
    }
}
