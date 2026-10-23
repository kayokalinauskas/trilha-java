package dev.trilhajava.m13.aulas.padroes;

/**
 * A interface que o checkout conhece. Ela fala a língua do domínio (solicitação e
 * resultado), não a de nenhum fornecedor. Adaptadores e decorators a implementam.
 */
public interface GatewayDePagamento {

    /**
     * Cobra a solicitação. Recusas de negócio voltam como {@link ResultadoDaCobranca.Recusada};
     * falhas que vale repetir sobem como {@link FalhaTemporariaException}.
     */
    ResultadoDaCobranca cobrar(SolicitacaoDeCobranca solicitacao);
}
