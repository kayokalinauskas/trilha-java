package dev.trilhajava.m02.frete;

public record Retirada(String loja) implements Entrega {

    public Retirada {
        if (loja == null || loja.isBlank()) {
            throw new IllegalArgumentException("Loja de retirada é obrigatória");
        }
        loja = loja.strip();
    }
}
