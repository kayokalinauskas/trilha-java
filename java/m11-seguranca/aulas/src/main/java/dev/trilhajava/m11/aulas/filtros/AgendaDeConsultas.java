package dev.trilhajava.m11.aulas.filtros;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/** Agenda em memória: o foco da aula é a segurança, não a persistência. */
@Component
public class AgendaDeConsultas {

    private final Map<Long, Consulta> consultas = new ConcurrentSkipListMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public AgendaDeConsultas() {
        marcar(new NovaConsulta("Marina Alves", "dra.helena", LocalDateTime.of(2026, 10, 20, 9, 0)));
    }

    public Consulta marcar(NovaConsulta nova) {
        long id = proximoId.getAndIncrement();
        Consulta consulta = new Consulta(id, nova.paciente(), nova.medico(), nova.horario());
        consultas.put(id, consulta);
        return consulta;
    }

    public List<Consulta> todas() {
        return List.copyOf(consultas.values());
    }
}
