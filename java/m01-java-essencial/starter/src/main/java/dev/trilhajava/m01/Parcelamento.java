package dev.trilhajava.m01;

import java.math.BigDecimal;

/**
 * Exercício 01.02 — Simulador de parcelamento.
 * Rode: mvn test -Dtest=ParcelamentoTest
 */
public final class Parcelamento {

    public static final int MAXIMO_PARCELAS = 24;

    private Parcelamento() {
    }

    /**
     * Divide o total em parcelas iguais, com 2 casas decimais.
     * Centavos que sobrarem vão para a PRIMEIRA parcela; a soma deve fechar exatamente o total.
     */
    public static BigDecimal[] semJuros(BigDecimal total, int parcelas) {
        throw new UnsupportedOperationException("TODO: implemente semJuros");
    }

    /**
     * Valor da parcela pela Tabela Price: PMT = PV * i / (1 - (1 + i)^-n), arredondado para 2 casas (HALF_EVEN).
     * Com taxa zero, é simplesmente PV / n.
     */
    public static BigDecimal parcelaComJuros(BigDecimal valor, BigDecimal taxaMensal, int parcelas) {
        throw new UnsupportedOperationException("TODO: implemente parcelaComJuros");
    }
}
