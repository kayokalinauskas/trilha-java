package dev.trilhajava.m11.aulas.filtros;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConsultaController {

    private final AgendaDeConsultas agenda;
    private final ServicoDeProntuario prontuarios;

    public ConsultaController(AgendaDeConsultas agenda, ServicoDeProntuario prontuarios) {
        this.agenda = agenda;
        this.prontuarios = prontuarios;
    }

    @GetMapping("/status")
    Map<String, String> status() {
        return Map.of("status", "ok");
    }

    @GetMapping("/consultas")
    List<Consulta> listar() {
        return agenda.todas();
    }

    @PostMapping("/consultas")
    ResponseEntity<Consulta> marcar(@Valid @RequestBody NovaConsulta nova, Authentication quem) {
        Consulta consulta = agenda.marcar(nova);
        return ResponseEntity.created(URI.create("/api/consultas/" + consulta.id()))
                .header("X-Marcada-Por", quem.getName())
                .body(consulta);
    }

    @GetMapping("/consultas/{id}/prontuario")
    Map<String, String> prontuario(@PathVariable long id) {
        return Map.of("resumo", prontuarios.resumo(id));
    }
}
