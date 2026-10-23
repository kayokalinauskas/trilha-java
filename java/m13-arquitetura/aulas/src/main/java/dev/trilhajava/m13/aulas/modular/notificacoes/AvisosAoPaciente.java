package dev.trilhajava.m13.aulas.modular.notificacoes;

import dev.trilhajava.m13.aulas.modular.agenda.eventos.ConsultaRealizada;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Avisa o paciente que a consulta foi registrada. Guarda os avisos em memória para a aula. */
@Component
public class AvisosAoPaciente {

    private final List<String> enviados = new CopyOnWriteArrayList<>();

    @ApplicationModuleListener
    void quandoRealizada(ConsultaRealizada evento) {
        enviados.add(evento.paciente() + ": sua consulta de " + evento.procedimento() + " foi registrada");
    }

    public List<String> enviados() {
        return List.copyOf(enviados);
    }
}
