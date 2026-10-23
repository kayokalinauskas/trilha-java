package dev.trilhajava.m04.credito;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

/**
 * Exercício 04.03 — Motor de regras de crédito.
 * Rode: mvn test -Dtest=MotorCreditoTest
 *
 * Cada regra é um Predicate<Proposta>. O motor não conhece nenhuma regra específica.
 */
public final class MotorCredito {

    private MotorCredito() {
    }

    public static Predicate<Proposta> idadeEntre(int minima, int maxima) {
        throw new UnsupportedOperationException("TODO: devolva uma lambda");
    }

    public static Predicate<Proposta> rendaMinima(BigDecimal minimo) {
        throw new UnsupportedOperationException("TODO");
    }

    public static Predicate<Proposta> scoreMinimo(int minimo) {
        throw new UnsupportedOperationException("TODO");
    }

    public static Predicate<Proposta> semNegativacao() {
        throw new UnsupportedOperationException("TODO: dica — Predicate.not ou method reference");
    }

    /** A parcela estimada não pode passar de percentual x renda mensal. */
    public static Predicate<Proposta> comprometimentoMaximo(BigDecimal percentual) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Política padrão, NESTA ORDEM: "idade" (18 a 75), "renda mínima" (1500), "score" (600),
     * "sem negativação", "comprometimento de renda" (30%).
     */
    public static List<Regra> politicaPadrao() {
        throw new UnsupportedOperationException("TODO");
    }

    public static Avaliacao avaliar(Proposta proposta, List<Regra> regras) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Um único predicado equivalente a "todas as regras aprovam". Lista vazia aprova tudo. */
    public static Predicate<Proposta> todas(List<Regra> regras) {
        throw new UnsupportedOperationException("TODO: dica — reduce");
    }

    /** score >= 850: 6x renda; >= 650: 3x; >= 500: 1x; abaixo: zero. */
    public static BigDecimal limiteSugerido(Proposta proposta) {
        throw new UnsupportedOperationException("TODO");
    }
}
