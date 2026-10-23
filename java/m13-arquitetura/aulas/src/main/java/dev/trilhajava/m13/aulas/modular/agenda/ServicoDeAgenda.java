package dev.trilhajava.m13.aulas.modular.agenda;

import dev.trilhajava.m13.aulas.modular.agenda.eventos.ConsultaRealizada;
import dev.trilhajava.m13.aulas.modular.agenda.internal.Consulta;
import dev.trilhajava.m13.aulas.modular.agenda.internal.Consultas;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A API pública do módulo agenda. */
@Service
public class ServicoDeAgenda {

    private final Consultas consultas;
    private final ApplicationEventPublisher eventos;

    ServicoDeAgenda(Consultas consultas, ApplicationEventPublisher eventos) {
        this.consultas = consultas;
        this.eventos = eventos;
    }

    public UUID agendar(String paciente, String procedimento, BigDecimal valor) {
        var consulta = new Consulta(UUID.randomUUID(), paciente, procedimento, valor, false);
        consultas.salvar(consulta);
        return consulta.id();
    }

    /**
     * Registra o atendimento e publica ConsultaRealizada. A agenda não sabe quem cobra nem
     * quem avisa o paciente: ela só anuncia o fato. A publicação é gravada no registro na
     * MESMA transação deste método.
     */
    @Transactional
    public void registrarAtendimento(UUID id) {
        var consulta = consultas.buscar(id)
                .orElseThrow(() -> new NoSuchElementException("consulta " + id + " não existe"))
                .realizar();
        consultas.salvar(consulta);
        eventos.publishEvent(new ConsultaRealizada(
                consulta.id(), consulta.paciente(), consulta.procedimento(), consulta.valor()));
    }

    public boolean realizada(UUID id) {
        return consultas.buscar(id).map(Consulta::realizada).orElse(false);
    }
}
