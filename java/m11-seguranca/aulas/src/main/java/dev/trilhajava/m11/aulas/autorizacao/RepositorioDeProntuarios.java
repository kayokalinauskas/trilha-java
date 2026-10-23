package dev.trilhajava.m11.aulas.autorizacao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * Repositório em memória, com os mesmos nomes que um repositório do Spring Data teria.
 * Repare na diferença entre porId (sem escopo: só a versão ingênua usa) e porIdNaClinica (escopada pelo tenant).
 */
@Repository
public class RepositorioDeProntuarios {

    private final Map<Long, Prontuario> porId = new ConcurrentSkipListMap<>();
    private final Map<Long, List<String>> exames = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong(1003);

    public RepositorioDeProntuarios() {
        salvar(new Prontuario(1001, "sol", "pac-ana", "med-carla",
                "Dor de cabeça recorrente", "Suspeita de enxaqueca; pedir ressonância."));
        salvar(new Prontuario(1002, "sol", "pac-bruno", "med-diego",
                "Check-up anual", "LDL 190 mg/dL; iniciar estatina."));
        salvar(new Prontuario(1003, "lua", "pac-julia", "med-heitor",
                "Alergia sazonal", "Prescrito anti-histamínico."));
        exames.put(1001L, List.of("Ressonância de crânio: sem alterações"));
        exames.put(1002L, List.of("Perfil lipídico: LDL 190 mg/dL"));
    }

    /** SEM escopo: devolve o prontuário de qualquer clínica. É a consulta que abre a porta para o BOLA. */
    public Optional<Prontuario> porId(long id) {
        return Optional.ofNullable(porId.get(id));
    }

    /** Equivale a findByIdAndClinicaId: o tenant faz parte da consulta. */
    public Optional<Prontuario> porIdNaClinica(long id, String clinicaId) {
        return porId(id).filter(p -> p.clinicaId().equals(clinicaId));
    }

    /** Equivale a findByClinicaIdAndPacienteId. */
    public List<Prontuario> doPaciente(String clinicaId, String pacienteId) {
        return porId.values().stream()
                .filter(p -> p.clinicaId().equals(clinicaId) && p.pacienteId().equals(pacienteId))
                .toList();
    }

    /** Equivale a findByClinicaIdAndMedicoId. */
    public List<Prontuario> doMedico(String clinicaId, String medicoId) {
        return porId.values().stream()
                .filter(p -> p.clinicaId().equals(clinicaId) && p.medicoId().equals(medicoId))
                .toList();
    }

    /** Equivale a findByClinicaId. */
    public List<Prontuario> daClinica(String clinicaId) {
        return porId.values().stream().filter(p -> p.clinicaId().equals(clinicaId)).toList();
    }

    public List<String> examesDo(long prontuarioId) {
        return exames.getOrDefault(prontuarioId, List.of());
    }

    public long proximoId() {
        return sequencia.incrementAndGet();
    }

    public Prontuario salvar(Prontuario prontuario) {
        porId.put(prontuario.id(), prontuario);
        return prontuario;
    }
}
