package dev.trilhajava.m11.aulas.autorizacao;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rota com o tenant no CAMINHO. Quem garante que {clinicaId} é a clínica do token é o MesmaClinicaDaRota,
 * configurado no SecurityFilterChain: este controller nem chega a rodar para outra clínica.
 */
@RestController
public class RelatorioController {

    private final RepositorioDeProntuarios repositorio;

    public RelatorioController(RepositorioDeProntuarios repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping("/clinicas/{clinicaId}/relatorios/atendimentos-por-medico")
    public Map<String, Long> atendimentosPorMedico(@PathVariable String clinicaId) {
        return repositorio.daClinica(clinicaId).stream()
                .collect(Collectors.groupingBy(Prontuario::medicoId, TreeMap::new, Collectors.counting()));
    }
}
