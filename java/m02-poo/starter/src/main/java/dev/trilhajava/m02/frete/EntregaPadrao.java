package dev.trilhajava.m02.frete;

public record EntregaPadrao(Cep cep, double pesoKg) implements Entrega {

    public EntregaPadrao {
        // TODO: cep obrigatório; peso > 0 e <= 30 kg. Evite repetir a regra de peso em 3 records!
    }
}
