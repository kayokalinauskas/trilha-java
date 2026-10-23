package dev.trilhajava.m10.frete;

import java.util.function.Supplier;

/**
 * Fábrica da calculadora. Nos testes do exercício 10.01, basta {@code new CalculadoraDeFrete()}.
 */
public final class Fretes {

    private static volatile Supplier<? extends CalculoDeFrete> fabrica = CalculadoraDeFrete::new;

    private Fretes() {
    }

    public static CalculoDeFrete calculadora() {
        return fabrica.get();
    }

    static void usar(Supplier<? extends CalculoDeFrete> outra) {
        fabrica = outra;
    }

    static void restaurar() {
        fabrica = CalculadoraDeFrete::new;
    }
}
