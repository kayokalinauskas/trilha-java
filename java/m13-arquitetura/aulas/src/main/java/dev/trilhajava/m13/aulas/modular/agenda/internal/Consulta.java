package dev.trilhajava.m13.aulas.modular.agenda.internal;

import java.math.BigDecimal;
import java.util.UUID;

/** A consulta como a agenda a enxerga. Interna: nenhum outro módulo conhece este tipo. */
public record Consulta(UUID id, String paciente, String procedimento, BigDecimal valor, boolean realizada) {

    public Consulta realizar() {
        if (realizada) {
            throw new IllegalStateException("consulta " + id + " já foi realizada");
        }
        return new Consulta(id, paciente, procedimento, valor, true);
    }
}
