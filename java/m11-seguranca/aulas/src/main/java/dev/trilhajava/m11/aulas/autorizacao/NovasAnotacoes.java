package dev.trilhajava.m11.aulas.autorizacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de entrada da edição de anotações: um campo, e só ele. */
public record NovasAnotacoes(@NotBlank @Size(max = 4000) String anotacoes) {}
