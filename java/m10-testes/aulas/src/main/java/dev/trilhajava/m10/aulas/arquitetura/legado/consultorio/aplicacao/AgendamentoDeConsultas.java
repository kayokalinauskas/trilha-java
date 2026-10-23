package dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.aplicacao;

import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda.Agenda;
import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda.Consulta;
import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.paciente.Paciente;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoDeConsultas {

    private final Agenda agenda;

    public AgendamentoDeConsultas(Agenda agenda) {
        this.agenda = agenda;
    }

    public Consulta agendar(Paciente paciente, LocalDateTime horario) {
        // Violação (tempo): o relógio do sistema, impossível de controlar num teste.
        if (horario.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("não dá para agendar no passado: " + horario);
        }
        if (!agenda.horarioLivre(horario)) {
            throw new IllegalStateException("horário ocupado: " + horario);
        }
        var consulta = new Consulta(paciente, horario);
        agenda.salvar(consulta);
        return consulta;
    }
}
