package dev.trilhajava.m02.frete;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Exercício 02.04 — calcule o frete com um switch exaustivo sobre a hierarquia sealed.
 * Rode: mvn test -Dtest=CalculadoraFreteTest
 */
public class CalculadoraFrete {

    /** Valor do frete com 2 casas decimais. */
    public BigDecimal calcular(Entrega entrega, BigDecimal valorPedido, LocalDate hoje) {
        throw new UnsupportedOperationException("TODO: use switch (entrega) { case Retirada r -> ... }");
    }

    /** Texto para o cliente — veja os exemplos no teste. Dica: record patterns desestruturam os componentes. */
    public String descrever(Entrega entrega) {
        throw new UnsupportedOperationException("TODO");
    }
}
