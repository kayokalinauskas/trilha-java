package dev.trilhajava.fintech.aplicacao;

import dev.trilhajava.fintech.dominio.Transferencia;

/**
 * Marco 6: porta de observabilidade do caso de uso. A aplicação diz O QUE aconteceu; a infra decide
 * COMO isso vira métrica e log (Micrometer, SLF4J). O caso de uso não conhece nenhum dos dois.
 */
public interface ObservadorDeTransferencias {

    /** Por que uma transferência foi recusada. Poucos valores fixos: cada um vira uma série no Prometheus. */
    enum Motivo { SALDO_INSUFICIENTE, CONTA_NAO_ENCONTRADA, INVALIDA }

    /** Chamado dentro da transação; a implementação só registra depois do commit. */
    void realizada(Transferencia transferencia);

    void recusada(Motivo motivo);
}
