package dev.trilhajava.m02.frete;

import java.time.LocalDate;

public record EntregaAgendada(Cep cep, double pesoKg, LocalDate data) implements Entrega {

    public EntregaAgendada {
        Entrega.exigirCep(cep);
        Entrega.validarPeso(pesoKg);
        if (data == null) {
            throw new IllegalArgumentException("Data do agendamento é obrigatória");
        }
    }
}
