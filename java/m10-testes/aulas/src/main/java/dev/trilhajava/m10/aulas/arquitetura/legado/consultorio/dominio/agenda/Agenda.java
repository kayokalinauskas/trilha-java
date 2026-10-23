package dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda;

import java.time.LocalDateTime;

public interface Agenda {

    boolean horarioLivre(LocalDateTime horario);

    void salvar(Consulta consulta);
}
