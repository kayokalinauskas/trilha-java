package dev.trilhajava.m02.frete;

public record Retirada(String loja) implements Entrega {

    public Retirada {
        // TODO: loja obrigatória (não nula, não em branco)
    }
}
