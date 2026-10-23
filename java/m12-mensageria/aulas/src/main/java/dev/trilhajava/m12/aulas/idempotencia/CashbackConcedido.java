package dev.trilhajava.m12.aulas.idempotencia;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * O evento que o serviço de compras publica quando uma compra gera cashback.
 *
 * @param eventoId identifica a MENSAGEM lógica: é o mesmo em toda reentrega
 * @param compraId a chave de NEGÓCIO: uma compra gera no máximo um cashback
 */
public record CashbackConcedido(String eventoId, String contaId, String compraId, BigDecimal valor) {

    public CashbackConcedido {
        Objects.requireNonNull(eventoId, "eventoId");
        Objects.requireNonNull(contaId, "contaId");
        Objects.requireNonNull(compraId, "compraId");
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("o valor do cashback precisa ser positivo");
        }
    }
}
