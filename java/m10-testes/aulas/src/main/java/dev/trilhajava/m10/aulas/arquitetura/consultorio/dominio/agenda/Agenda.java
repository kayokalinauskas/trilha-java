package dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.agenda;

import java.time.LocalDateTime;

/** A porta: o domínio diz o que precisa; a persistência decide como guardar. */
public interface Agenda {

    boolean horarioLivre(LocalDateTime horario);

    void salvar(Consulta consulta);
}
