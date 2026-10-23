package dev.trilhajava.m16.classificador;

/** O que o modelo devolve (em JSON) e o que o classificador entrega: a categoria e a confiança, de 0 a 1. */
public record Classificacao(Categoria categoria, double confianca) {

    /** A resposta "não sei": usada quando o modelo falha ou devolve algo que não dá para aproveitar. */
    public static Classificacao naoClassificado() {
        return new Classificacao(Categoria.OUTROS, 0.0);
    }
}
