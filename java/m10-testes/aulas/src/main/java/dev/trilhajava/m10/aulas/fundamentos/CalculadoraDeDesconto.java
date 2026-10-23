package dev.trilhajava.m10.aulas.fundamentos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calcula o desconto de uma compra na loja de cafés.
 *
 * <ol>
 *   <li>Subtotal e cliente são obrigatórios; subtotal negativo lança {@link IllegalArgumentException}.</li>
 *   <li>Faixas pelo subtotal: abaixo de R$ 100,00, 0%; a partir de R$ 100,00, 5%; a partir de R$ 300,00, 10%.</li>
 *   <li>O assinante ganha mais 5 pontos percentuais em qualquer faixa (5%, 10% ou 15%).</li>
 *   <li>O desconto é arredondado para centavos com HALF_UP (5,005 vira 5,01).</li>
 *   <li>O desconto nunca passa de R$ 50,00.</li>
 * </ol>
 */
public final class CalculadoraDeDesconto {

    static final BigDecimal INICIO_FAIXA_5 = new BigDecimal("100.00");
    static final BigDecimal INICIO_FAIXA_10 = new BigDecimal("300.00");
    static final BigDecimal EXTRA_ASSINANTE = new BigDecimal("0.05");
    static final BigDecimal TETO = new BigDecimal("50.00");

    public BigDecimal calcular(BigDecimal subtotal, Cliente cliente) {
        Objects.requireNonNull(subtotal, "subtotal");
        Objects.requireNonNull(cliente, "cliente");
        if (subtotal.signum() < 0) {
            throw new IllegalArgumentException("subtotal não pode ser negativo: " + subtotal);
        }

        BigDecimal percentual = percentualDaFaixa(subtotal);
        if (cliente == Cliente.ASSINANTE) {
            percentual = percentual.add(EXTRA_ASSINANTE);
        }

        BigDecimal desconto = subtotal.multiply(percentual).setScale(2, RoundingMode.HALF_UP);
        return desconto.min(TETO);
    }

    private static BigDecimal percentualDaFaixa(BigDecimal subtotal) {
        if (subtotal.compareTo(INICIO_FAIXA_10) >= 0) {
            return new BigDecimal("0.10");
        }
        if (subtotal.compareTo(INICIO_FAIXA_5) >= 0) {
            return new BigDecimal("0.05");
        }
        return BigDecimal.ZERO;
    }
}
