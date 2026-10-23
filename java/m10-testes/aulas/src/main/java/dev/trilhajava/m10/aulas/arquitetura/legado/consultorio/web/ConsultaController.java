package dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.web;

import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.aplicacao.AgendamentoDeConsultas;
import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.dominio.agenda.Consulta;
import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.persistencia.AgendaJdbc;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Violação (camadas): "só desta vez", o controller lê direto da persistência. */
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final AgendamentoDeConsultas agendamento;
    private final AgendaJdbc agenda;

    public ConsultaController(AgendamentoDeConsultas agendamento, AgendaJdbc agenda) {
        this.agendamento = agendamento;
        this.agenda = agenda;
    }

    @GetMapping
    public List<Consulta> listar() {
        return agenda.todas();
    }
}
