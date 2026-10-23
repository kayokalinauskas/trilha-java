package dev.trilhajava.m10.aulas.arquitetura.consultorio.persistencia;

import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.agenda.Agenda;
import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.agenda.Consulta;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AgendaEmMemoria implements Agenda {

    private final Map<LocalDateTime, Consulta> consultas = new ConcurrentHashMap<>();

    @Override
    public boolean horarioLivre(LocalDateTime horario) {
        return !consultas.containsKey(horario);
    }

    @Override
    public void salvar(Consulta consulta) {
        consultas.put(consulta.horario(), consulta);
    }
}
