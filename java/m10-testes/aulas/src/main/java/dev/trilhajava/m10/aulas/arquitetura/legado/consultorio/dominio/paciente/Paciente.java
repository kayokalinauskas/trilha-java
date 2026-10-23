package dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.paciente;

import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda.Consulta;
import java.util.List;

/** Violação (ciclo): paciente conhece consulta, e consulta conhece paciente. */
public record Paciente(String id, String nome, List<Consulta> historico) {
}
