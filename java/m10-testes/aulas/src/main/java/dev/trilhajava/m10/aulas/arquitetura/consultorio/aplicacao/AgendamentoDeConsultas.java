package dev.trilhajava.m10.aulas.arquitetura.consultorio.aplicacao;

import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.agenda.Agenda;
import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.agenda.Consulta;
import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.paciente.Paciente;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoDeConsultas {

    private final Agenda agenda;
    private final Clock relogio;

    public AgendamentoDeConsultas(Agenda agenda, Clock relogio) {
        this.agenda = agenda;
        this.relogio = relogio;
    }

    public Consulta agendar(Paciente paciente, LocalDateTime horario) {
        if (horario.isBefore(LocalDateTime.now(relogio))) {
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
