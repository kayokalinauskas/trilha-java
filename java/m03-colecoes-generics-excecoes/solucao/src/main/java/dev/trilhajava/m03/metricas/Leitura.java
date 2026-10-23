package dev.trilhajava.m03.metricas;

import java.time.Instant;

/** Uma leitura de sensor. T pode ser Integer (temperatura), Double (umidade), BigDecimal... Já pronto. */
public record Leitura<T extends Number>(String sensor, Instant momento, T valor) {
}
