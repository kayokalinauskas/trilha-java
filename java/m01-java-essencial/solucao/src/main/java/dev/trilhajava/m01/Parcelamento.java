package dev.trilhajava.m01;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Solução de referência do exercício 01.02. */
public final class Parcelamento {

    public static final int MAXIMO_PARCELAS = 24;
    private static final MathContext PRECISAO = MathContext.DECIMAL64;

    private Parcelamento() {
    }

    public static BigDecimal[] semJuros(BigDecimal total, int parcelas) {
        validar(total, parcelas);
        // Trabalhar em centavos (inteiros) elimina qualquer dúvida de arredondamento.
        long centavos = total.setScale(2, RoundingMode.HALF_EVEN).movePointRight(2).longValueExact();
        long base = centavos / parcelas;
        long sobra = centavos % parcelas;

        BigDecimal[] resultado = new BigDecimal[parcelas];
        for (int i = 0; i < parcelas; i++) {
            long valor = (i == 0) ? base + sobra : base;
            resultado[i] = BigDecimal.valueOf(valor, 2);
        }
        return resultado;
    }

    public static BigDecimal parcelaComJuros(BigDecimal valor, BigDecimal taxaMensal, int parcelas) {
        validar(valor, parcelas);
        if (taxaMensal == null || taxaMensal.signum() < 0) {
            throw new IllegalArgumentException("A taxa mensal deve ser zero ou positiva");
        }
        if (taxaMensal.signum() == 0) {
            return valor.divide(BigDecimal.valueOf(parcelas), 2, RoundingMode.HALF_EVEN);
        }
        // (1 + i)^n
        BigDecimal fator = BigDecimal.ONE.add(taxaMensal).pow(parcelas, PRECISAO);
        // PMT = PV * i * (1+i)^n / ((1+i)^n - 1)  — forma equivalente, sem expoente negativo
        BigDecimal numerador = valor.multiply(taxaMensal, PRECISAO).multiply(fator, PRECISAO);
        BigDecimal denominador = fator.subtract(BigDecimal.ONE, PRECISAO);
        return numerador.divide(denominador, 2, RoundingMode.HALF_EVEN);
    }

    private static void validar(BigDecimal total, int parcelas) {
        if (total == null || total.signum() <= 0) {
            throw new IllegalArgumentException("O total deve ser maior que zero");
        }
        if (parcelas < 1 || parcelas > MAXIMO_PARCELAS) {
            throw new IllegalArgumentException("Número de parcelas deve estar entre 1 e " + MAXIMO_PARCELAS + ", recebido: " + parcelas);
        }
    }
}
