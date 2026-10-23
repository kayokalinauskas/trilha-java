package dev.trilhajava.m13.aulas.hexagonal.dominio;

/** Quantidade de pontos de fidelidade. Objeto de valor: imutável e nunca negativo. */
public record Pontos(long quantidade) {

    public static final Pontos ZERO = new Pontos(0);

    public Pontos {
        if (quantidade < 0) {
            throw new IllegalArgumentException("pontos não podem ser negativos: " + quantidade);
        }
    }

    public static Pontos de(long quantidade) {
        return new Pontos(quantidade);
    }

    public Pontos mais(Pontos outros) {
        return new Pontos(Math.addExact(quantidade, outros.quantidade));
    }

    public Pontos menos(Pontos outros) {
        return new Pontos(quantidade - outros.quantidade);   // o construtor recusa resultado negativo
    }

    public boolean menorQue(Pontos outros) {
        return quantidade < outros.quantidade;
    }
}
