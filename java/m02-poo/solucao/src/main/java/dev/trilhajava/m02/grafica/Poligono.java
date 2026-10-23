package dev.trilhajava.m02.grafica;

import java.util.List;
import java.util.Locale;

/** Polígono simples (sem autointerseção); área pela fórmula do shoelace (Gauss). */
public final class Poligono implements Forma {

    private final List<Ponto> vertices;
    private final double area;

    public Poligono(List<Ponto> vertices) {
        if (vertices == null || vertices.size() < 3) {
            throw new IllegalArgumentException("Um polígono precisa de pelo menos 3 vértices");
        }
        this.vertices = List.copyOf(vertices); // cópia defensiva e imutável
        this.area = shoelace(this.vertices);
        if (area <= 1e-12) {
            throw new IllegalArgumentException("Os vértices não formam uma figura com área (pontos colineares?)");
        }
    }

    /** Soma de (x_i * y_(i+1) - x_(i+1) * y_i), fechando no primeiro vértice; área = |soma| / 2. */
    private static double shoelace(List<Ponto> pontos) {
        double soma = 0;
        for (int i = 0; i < pontos.size(); i++) {
            Ponto atual = pontos.get(i);
            Ponto proximo = pontos.get((i + 1) % pontos.size());
            soma += atual.x() * proximo.y() - proximo.x() * atual.y();
        }
        return Math.abs(soma) / 2;
    }

    @Override
    public double area() {
        return area; // calculada uma vez: o polígono é imutável
    }

    @Override
    public String descricao() {
        return String.format(Locale.ROOT, "Polígono de %d vértices", vertices.size());
    }
}
