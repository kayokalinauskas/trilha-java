package dev.trilhajava.m04.credito;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

/** Solução de referência do exercício 04.03. */
public final class MotorCredito {

    private MotorCredito() {
    }

    public static Predicate<Proposta> idadeEntre(int minima, int maxima) {
        return p -> p.idade() >= minima && p.idade() <= maxima;   // captura minima/maxima (closure)
    }

    public static Predicate<Proposta> rendaMinima(BigDecimal minimo) {
        return p -> p.rendaMensal().compareTo(minimo) >= 0;
    }

    public static Predicate<Proposta> scoreMinimo(int minimo) {
        return p -> p.score() >= minimo;
    }

    public static Predicate<Proposta> semNegativacao() {
        return Predicate.not(Proposta::negativado);
    }

    public static Predicate<Proposta> comprometimentoMaximo(BigDecimal percentual) {
        return p -> p.parcelaEstimada().compareTo(p.rendaMensal().multiply(percentual)) <= 0;
    }

    public static List<Regra> politicaPadrao() {
        return List.of(
                new Regra("idade", idadeEntre(18, 75)),
                new Regra("renda mínima", rendaMinima(new BigDecimal("1500"))),
                new Regra("score", scoreMinimo(600)),
                new Regra("sem negativação", semNegativacao()),
                new Regra("comprometimento de renda", comprometimentoMaximo(new BigDecimal("0.30"))));
    }

    public static Avaliacao avaliar(Proposta proposta, List<Regra> regras) {
        List<String> reprovadas = regras.stream()
                .filter(regra -> !regra.aprova(proposta))
                .map(Regra::nome)
                .toList();
        return new Avaliacao(reprovadas.isEmpty(), reprovadas);
    }

    public static Predicate<Proposta> todas(List<Regra> regras) {
        return regras.stream()
                .map(Regra::condicao)
                .reduce(p -> true, Predicate::and);   // identidade do "and" é o predicado sempre verdadeiro
    }

    public static BigDecimal limiteSugerido(Proposta proposta) {
        return proposta.rendaMensal().multiply(BigDecimal.valueOf(multiplicadorPorScore(proposta.score())));
    }

    private static int multiplicadorPorScore(int score) {
        if (score >= 850) {
            return 6;
        }
        if (score >= 650) {
            return 3;
        }
        if (score >= 500) {
            return 1;
        }
        return 0;
    }
}
