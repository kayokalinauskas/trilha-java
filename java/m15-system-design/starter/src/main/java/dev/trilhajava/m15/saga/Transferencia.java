package dev.trilhajava.m15.saga;

import java.math.BigDecimal;

public record Transferencia(String id, String origem, String contaDestino, BigDecimal valor, Estado estado) {

    public enum Estado {
        /** O parceiro aceitou: dinheiro saiu daqui e chegou lá. */
        CONCLUIDA,
        /** Sem saldo: nada foi debitado nem enviado. */
        RECUSADA,
        /** O parceiro recusou (ou nunca recebeu): o débito foi estornado. */
        COMPENSADA,
        /** Não sabemos o que aconteceu lá: o débito fica, até a reconciliação descobrir. */
        PENDENTE
    }

    Transferencia com(Estado novo) {
        return new Transferencia(id, origem, contaDestino, valor, novo);
    }
}
