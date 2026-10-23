package dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda;

import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.paciente.Paciente;
import java.time.LocalDateTime;
import org.springframework.util.Assert;

/** Violação (domínio com Spring): um atalho "só para validar" amarra o domínio ao framework. */
public record Consulta(Paciente paciente, LocalDateTime horario) {

    public Consulta {
        Assert.notNull(paciente, "paciente");
        Assert.notNull(horario, "horario");
    }
}
