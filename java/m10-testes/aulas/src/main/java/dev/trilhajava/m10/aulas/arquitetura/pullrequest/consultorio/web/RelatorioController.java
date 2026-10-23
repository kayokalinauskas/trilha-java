package dev.trilhajava.m10.aulas.arquitetura.pullrequest.consultorio.web;

import dev.trilhajava.m10.aulas.arquitetura.legado.consultorio.persistencia.AgendaJdbc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * O pull request de hoje, no sistema legado: um controller NOVO que repete o atalho antigo
 * (web lendo direto da persistência). A regra congelada tolera as violações antigas, não esta.
 */
@RestController
public class RelatorioController {

    private final AgendaJdbc agenda;

    public RelatorioController(AgendaJdbc agenda) {
        this.agenda = agenda;
    }

    @GetMapping("/relatorios/consultas")
    public int totalDeConsultas() {
        return agenda.todas().size();
    }
}
