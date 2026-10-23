package dev.trilhajava.m04.eventos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Exercício 04.04 — Projeção de conta (event sourcing).
 * Rode: mvn test -Dtest=ProjecaoContaTest
 */
public final class ProjecaoConta {

    private ProjecaoConta() {
    }

    /**
     * Ordena os eventos de UMA conta pelo momento e os aplica a partir de um estado vazio.
     * O primeiro evento precisa ser ContaAberta (senão IllegalStateException).
     * Eventos de contas diferentes: IllegalArgumentException.
     */
    public static EstadoConta reconstruir(List<EventoConta> eventos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Estado de cada conta, chaves em ordem alfabética. */
    public static Map<String, EstadoConta> reconstruirTodas(List<EventoConta> eventos) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Texto do extrato — use switch com record patterns. */
    public static String descrever(EventoConta evento) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Soma de depósitos, saques e transferências recebidas no dia (no fuso informado). */
    public static BigDecimal totalMovimentado(List<EventoConta> eventos, LocalDate dia, ZoneId fuso) {
        throw new UnsupportedOperationException("TODO");
    }
}
