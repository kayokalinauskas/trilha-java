package dev.trilhajava.m08.pacientes;

import java.time.LocalDate;

/** O paciente como a aplicação o guarda (modelo interno — nunca sai direto pela API). */
public record Paciente(long id, String nome, String cpf, String email, LocalDate nascimento) {
}
