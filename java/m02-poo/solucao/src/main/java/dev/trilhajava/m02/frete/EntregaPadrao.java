package dev.trilhajava.m02.frete;

public record EntregaPadrao(Cep cep, double pesoKg) implements Entrega {

    public EntregaPadrao {
        Entrega.exigirCep(cep);
        Entrega.validarPeso(pesoKg);
    }
}
