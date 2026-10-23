package dev.trilhajava.m08.pacientes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Corpo de POST e PUT. As anotações são o contrato de entrada: o Spring valida antes
 * de chamar o controller (por causa do @Valid) e responde 400 se algo estiver errado.
 */
public record DadosPaciente(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @CpfValido String cpf,
        @Email @Size(max = 254) String email,
        @NotNull @Past LocalDate nascimento) {
}
