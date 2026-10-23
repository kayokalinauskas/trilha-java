package dev.trilhajava.m02.grafica;

import java.util.Locale;

public final class Retangulo implements Forma {

    private final double largura;
    private final double altura;

    public Retangulo(double largura, double altura) {
        this.largura = Dimensoes.positiva(largura, "Largura");
        this.altura = Dimensoes.positiva(altura, "Altura");
    }

    @Override
    public double area() {
        return largura * altura;
    }

    @Override
    public String descricao() {
        return String.format(Locale.ROOT, "Retângulo (%.2f x %.2f m)", largura, altura);
    }
}
