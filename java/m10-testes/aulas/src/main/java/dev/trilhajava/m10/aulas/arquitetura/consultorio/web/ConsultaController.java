package dev.trilhajava.m10.aulas.arquitetura.consultorio.web;

import dev.trilhajava.m10.aulas.arquitetura.consultorio.aplicacao.AgendamentoDeConsultas;
import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.agenda.Consulta;
import dev.trilhajava.m10.aulas.arquitetura.consultorio.dominio.paciente.Paciente;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final AgendamentoDeConsultas agendamento;

    public ConsultaController(AgendamentoDeConsultas agendamento) {
        this.agendamento = agendamento;
    }

    @PostMapping
    public Consulta agendar(@RequestBody NovaConsulta pedido) {
        return agendamento.agendar(new Paciente(pedido.pacienteId(), pedido.nome()), pedido.horario());
    }

    public record NovaConsulta(String pacienteId, String nome, LocalDateTime horario) {
    }
}
