package dev.trilhajava.m02.grafica;

import java.math.BigDecimal;

/** Regra de preço separada do cálculo de área. Já pronta. */
public interface Precificacao {

    /** Preço (2 casas decimais, HALF_EVEN) para uma área total em m². */
    BigDecimal precoPara(double areaTotalM2);
}
