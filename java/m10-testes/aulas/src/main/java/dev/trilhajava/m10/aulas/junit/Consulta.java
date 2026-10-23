package dev.trilhajava.m10.aulas.junit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Uma consulta marcada. Toda consulta dura {@link Agenda#DURACAO}. */
public record Consulta(String id, String paciente, Especialidade especialidade, Convenio convenio,
        LocalDateTime inicio, BigDecimal valor) {

    public LocalDateTime fim() {
        return inicio.plus(Agenda.DURACAO);
    }
}
