package dev.trilhajava.m13.frete;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Uma opção de frete. O valor é sempre normalizado para 2 casas (HALF_EVEN). */
public record Cotacao(String modalidade, BigDecimal valor, int prazoEmDiasUteis) {

    public Cotacao {
        Objects.requireNonNull(modalidade, "modalidade");
        valor = Objects.requireNonNull(valor, "valor").setScale(2, RoundingMode.HALF_EVEN);
        if (valor.signum() < 0 || prazoEmDiasUteis < 0) {
            throw new IllegalArgumentException("valor e prazo não podem ser negativos");
        }
    }

    public Cotacao comValor(BigDecimal novoValor) {
        return new Cotacao(modalidade, novoValor, prazoEmDiasUteis);
    }
}
