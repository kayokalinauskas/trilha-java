package dev.trilhajava.m04.credito;

import java.math.BigDecimal;

/** Proposta de crédito pessoal. Já pronta. */
public record Proposta(String cpf, int idade, BigDecimal rendaMensal, int score, BigDecimal valorSolicitado, int parcelas,
        boolean negativado) {

    /** Valor aproximado da parcela (sem juros), usado nas regras de comprometimento de renda. */
    public BigDecimal parcelaEstimada() {
        return valorSolicitado.divide(BigDecimal.valueOf(parcelas), 2, java.math.RoundingMode.HALF_EVEN);
    }
}
