package dev.trilhajava.m11.aulas.filtros;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** O corpo de POST /api/consultas. */
public record NovaConsulta(@NotBlank String paciente, @NotBlank String medico, @NotNull @Future LocalDateTime horario) {
}
