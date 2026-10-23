package dev.trilhajava.m10.aulas.junit;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cada convênio dá um desconto percentual sobre o valor cheio da consulta. */
public enum Convenio {
    PARTICULAR(0),
    BASICO(15),
    PREMIUM(35);

    private final int percentualDeDesconto;

    Convenio(int percentualDeDesconto) {
        this.percentualDeDesconto = percentualDeDesconto;
    }

    /** Aplica o desconto e arredonda para centavos (HALF_UP). */
    public BigDecimal aplicarDesconto(BigDecimal valorCheio) {
        BigDecimal fator = BigDecimal.valueOf(100 - percentualDeDesconto).movePointLeft(2);
        return valorCheio.multiply(fator).setScale(2, RoundingMode.HALF_UP);
    }
}
