package dev.trilhajava.m12.pagamentos;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Um evento enviado pelo gateway de pagamentos. A entrega é "pelo menos uma vez": o mesmo evento
 * pode chegar duas vezes (mesmo mensagemId), e eventos podem chegar fora de ordem.
 *
 * @param mensagemId  identificador único da mensagem, definido pelo gateway
 * @param sequencia   cresce a cada evento do mesmo pedido: o maior é o mais recente
 */
public record EventoDePagamento(
        String mensagemId, String pedidoId, Tipo tipo, BigDecimal valor, String transacao, long sequencia) {

    public enum Tipo { APROVADO, ESTORNADO }

    public EventoDePagamento {
        if (mensagemId == null || mensagemId.isBlank()) {
            throw new IllegalArgumentException("mensagemId obrigatório");
        }
        Objects.requireNonNull(pedidoId, "pedidoId");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(transacao, "transacao");
    }
}
