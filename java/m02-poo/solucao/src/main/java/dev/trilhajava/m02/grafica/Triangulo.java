package dev.trilhajava.m02.grafica;

import java.util.Locale;

public final class Triangulo implements Forma {

    private final double base;
    private final double altura;

    public Triangulo(double base, double altura) {
        this.base = Dimensoes.positiva(base, "Base");
        this.altura = Dimensoes.positiva(altura, "Altura");
    }

    @Override
    public double area() {
        return base * altura / 2;
    }

    @Override
    public String descricao() {
        return String.format(Locale.ROOT, "Triângulo (base %.2f, altura %.2f m)", base, altura);
    }
}
