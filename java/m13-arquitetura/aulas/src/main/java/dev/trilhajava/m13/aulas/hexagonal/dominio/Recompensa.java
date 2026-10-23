package dev.trilhajava.m13.aulas.hexagonal.dominio;

/** Um item do catálogo que pode ser trocado por pontos (um café, um frete grátis, um ingresso). */
public record Recompensa(String codigo, String descricao, Pontos custo, boolean ativa) {
}
