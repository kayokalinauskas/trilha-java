package dev.trilhajava.m02.grafica;

import java.util.Locale;

public final class Circulo implements Forma {

    private final double raio;

    public Circulo(double raio) {
        this.raio = Dimensoes.positiva(raio, "Raio");
    }

    @Override
    public double area() {
        return Math.PI * raio * raio;
    }

    @Override
    public String descricao() {
        return String.format(Locale.ROOT, "Círculo (raio %.2f m)", raio);
    }
}
