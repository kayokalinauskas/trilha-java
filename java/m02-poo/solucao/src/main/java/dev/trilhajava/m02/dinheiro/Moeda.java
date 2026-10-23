package dev.trilhajava.m02.dinheiro;

/** Moedas suportadas, com o símbolo usado na formatação. */
public enum Moeda {
    BRL("R$"),
    USD("US$"),
    EUR("€");

    private final String simbolo;

    Moeda(String simbolo) {
        this.simbolo = simbolo;
    }

    public String simbolo() {
        return simbolo;
    }
}
