package dev.trilhajava.m02.frete;

import java.time.LocalDate;

public record EntregaAgendada(Cep cep, double pesoKg, LocalDate data) implements Entrega {

    public EntregaAgendada {
        // TODO: mesmas validações da entrega padrão + data obrigatória.
        // A regra de antecedência depende de "hoje", então ela fica na CalculadoraFrete.
    }
}
