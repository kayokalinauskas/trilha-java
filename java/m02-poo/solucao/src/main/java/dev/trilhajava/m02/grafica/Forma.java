package dev.trilhajava.m02.grafica;

/**
 * Abstração comum a todo formato de adesivo. Já pronta: o contrato é seu ponto de partida.
 * Implemente Circulo, Retangulo, Triangulo e Poligono.
 */
public interface Forma {

    /** Área em metros quadrados. Sempre maior que zero. */
    double area();

    /** Texto legível para o cliente, ex.: "Círculo (raio 0.50 m)". */
    String descricao();
}
