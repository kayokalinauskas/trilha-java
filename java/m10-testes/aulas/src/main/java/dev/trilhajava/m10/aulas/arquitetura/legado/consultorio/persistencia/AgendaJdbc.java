package dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.persistencia;

import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda.Agenda;
import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda.Consulta;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Violação (convenção): classe de persistência sem @Repository. (Em memória para o exemplo não precisar de banco.) */
public class AgendaJdbc implements Agenda {

    private final List<Consulta> consultas = new ArrayList<>();

    @Override
    public boolean horarioLivre(LocalDateTime horario) {
        return consultas.stream().noneMatch(c -> c.horario().equals(horario));
    }

    @Override
    public void salvar(Consulta consulta) {
        consultas.add(consulta);
    }

    public List<Consulta> todas() {
        return List.copyOf(consultas);
    }
}
