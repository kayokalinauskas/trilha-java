package dev.trilhajava.m11.aulas.filtros;

import java.time.LocalDateTime;

/** Uma consulta marcada na agenda da clínica. */
public record Consulta(long id, String paciente, String medico, LocalDateTime horario) {
}
