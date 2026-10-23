package dev.trilhajava.m04.eventos;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Eventos imutáveis do histórico de uma conta (event sourcing). Já prontos.
 * O estado atual da conta NÃO é guardado: ele é reconstruído aplicando os eventos em ordem.
 */
public sealed interface EventoConta {

    String contaId();

    Instant momento();

    record ContaAberta(String contaId, Instant momento, String titular) implements EventoConta { }

    record Deposito(String contaId, Instant momento, BigDecimal valor) implements EventoConta { }

    record Saque(String contaId, Instant momento, BigDecimal valor) implements EventoConta { }

    record TransferenciaRecebida(String contaId, Instant momento, BigDecimal valor, String origem) implements EventoConta { }

    record ContaBloqueada(String contaId, Instant momento, String motivo) implements EventoConta { }
}
