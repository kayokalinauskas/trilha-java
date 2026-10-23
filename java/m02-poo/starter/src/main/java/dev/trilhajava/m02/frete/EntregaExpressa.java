package dev.trilhajava.m02.frete;

public record EntregaExpressa(Cep cep, double pesoKg, boolean mesmoDia) implements Entrega {

    public EntregaExpressa {
        // TODO: mesmas validações da entrega padrão
    }
}
