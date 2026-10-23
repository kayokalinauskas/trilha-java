package dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.agenda;

import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.paciente.Paciente;
import java.time.LocalDateTime;
import java.util.Objects;

public record Consulta(Paciente paciente, LocalDateTime horario) {

    public Consulta {
        Objects.requireNonNull(paciente, "paciente");
        Objects.requireNonNull(horario, "horario");
    }
}
