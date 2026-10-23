package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.dominio.TipoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NovaContaRequest(
        @NotBlank @Size(max = 120) String titular,
        @NotBlank @CpfValido String cpf,
        @NotNull TipoConta tipo) {
}
