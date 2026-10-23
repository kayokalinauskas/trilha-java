package dev.trilhajava.m13.reservas.dominio;

import java.util.Objects;
import java.util.UUID;

/**
 * A reserva de uma sala. As regras que não dependem de outras reservas moram aqui, em Java puro.
 * Os requisitos e as regras estão no enunciado do exercício 13.02, no site.
 */
public class Reserva {

    private final UUID id;
    private final String sala;
    private final String responsavel;
    private final int participantes;
    private final Periodo periodo;
    private StatusDaReserva status;

    public Reserva(UUID id, String sala, String responsavel, int participantes, Periodo periodo,
            StatusDaReserva status) {
        this.id = Objects.requireNonNull(id);
        this.sala = Objects.requireNonNull(sala);
        this.responsavel = Objects.requireNonNull(responsavel);
        this.participantes = participantes;
        this.periodo = Objects.requireNonNull(periodo);
        this.status = Objects.requireNonNull(status);
    }

    // Implemente aqui o exercício: nova(...), cancelar(...) e conflitaCom(...), com as assinaturas do enunciado

    public boolean ativa() {
        return status == StatusDaReserva.ATIVA;
    }

    public UUID id() {
        return id;
    }

    public String sala() {
        return sala;
    }

    public String responsavel() {
        return responsavel;
    }

    public int participantes() {
        return participantes;
    }

    public Periodo periodo() {
        return periodo;
    }

    public StatusDaReserva status() {
        return status;
    }
}
