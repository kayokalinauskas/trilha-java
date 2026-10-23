package dev.trilhajava.m06;

import java.math.BigDecimal;

/** API externa (lenta e às vezes instável). Já pronta. */
public interface Transportadora {

    String nome();

    /** Chamada bloqueante: pode demorar segundos ou lançar exceção. */
    BigDecimal cotar(String cepDestino, double pesoKg);
}
