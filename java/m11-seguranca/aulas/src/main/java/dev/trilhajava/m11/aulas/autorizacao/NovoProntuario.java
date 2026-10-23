package dev.trilhajava.m11.aulas.autorizacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de ENTRADA do cadastro. Só os campos que a recepção pode escolher.
 * Não há clinicaId (vem do token), nem id (vem da sequência), nem anotacoes (só o médico escreve).
 */
public record NovoProntuario(
        @NotBlank String pacienteId,
        @NotBlank String medicoId,
        @NotBlank @Size(max = 500) String queixa) {}
