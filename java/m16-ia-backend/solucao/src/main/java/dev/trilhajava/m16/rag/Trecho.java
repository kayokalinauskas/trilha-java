package dev.trilhajava.m16.rag;

/** Um pedaço de documento devolvido pela busca, com a similaridade (cosseno) em relação à pergunta. */
public record Trecho(String documentoId, int indice, String texto, double similaridade) {
}
