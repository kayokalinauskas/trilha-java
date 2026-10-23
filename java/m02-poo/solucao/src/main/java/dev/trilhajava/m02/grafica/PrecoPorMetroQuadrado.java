package dev.trilhajava.m02.grafica;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PrecoPorMetroQuadrado implements Precificacao {

    private final BigDecimal precoPorM2;

    public PrecoPorMetroQuadrado(BigDecimal precoPorM2) {
        if (precoPorM2 == null || precoPorM2.signum() <= 0) {
            throw new IllegalArgumentException("Preço por m² deve ser maior que zero");
        }
        this.precoPorM2 = precoPorM2;
    }

    @Override
    public BigDecimal precoPara(double areaTotalM2) {
        // BigDecimal.valueOf(double) usa a representação decimal curta do double,
        // evitando 0.1 virar 0.1000000000000000055511151231257827...
        return precoPorM2.multiply(BigDecimal.valueOf(areaTotalM2)).setScale(2, RoundingMode.HALF_EVEN);
    }
}
