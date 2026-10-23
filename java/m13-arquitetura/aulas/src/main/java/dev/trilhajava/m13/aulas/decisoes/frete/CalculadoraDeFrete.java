package dev.trilhajava.m13.aulas.decisoes.frete;

import java.math.BigDecimal;

/**
 * A abstração do "branch by abstraction": o resto do sistema só conhece esta interface.
 * Atrás dela, a implementação antiga e a nova convivem até a migração terminar.
 */
public interface CalculadoraDeFrete {

    BigDecimal calcular(Remessa remessa);
}
