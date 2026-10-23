package dev.trilhajava.m13.reservas.dominio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A reserva de uma sala. Todas as regras que não dependem de outras reservas moram aqui,
 * em Java puro: nada de Spring, banco ou HTTP.
 */
public class Reserva {

    static final LocalTime ABRE = LocalTime.of(8, 0);
    static final LocalTime FECHA = LocalTime.of(20, 0);
    static final Duration MINIMO = Duration.ofMinutes(30);
    static final Duration MAXIMO = Duration.ofHours(4);

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

    /** Cria uma reserva ATIVA, validando as regras de horário, duração e capacidade. */
    public static Reserva nova(Sala sala, String responsavel, int participantes, Periodo periodo, LocalDateTime agora) {
        if (responsavel == null || responsavel.isBlank()) {
            throw new ReservaInvalidaException("o responsável é obrigatório");
        }
        if (!periodo.inicio().isAfter(agora)) {
            throw new ReservaInvalidaException("não dá para reservar no passado");
        }
        if (!periodo.inicio().toLocalDate().equals(periodo.fim().toLocalDate())) {
            throw new ReservaInvalidaException("a reserva precisa começar e terminar no mesmo dia");
        }
        if (periodo.inicio().toLocalTime().isBefore(ABRE) || periodo.fim().toLocalTime().isAfter(FECHA)) {
            throw new ReservaInvalidaException("o prédio funciona das 8h às 20h");
        }
        if (!multiploDe15(periodo.inicio()) || !multiploDe15(periodo.fim())) {
            throw new ReservaInvalidaException("início e fim em múltiplos de 15 minutos");
        }
        if (periodo.duracao().compareTo(MINIMO) < 0 || periodo.duracao().compareTo(MAXIMO) > 0) {
            throw new ReservaInvalidaException("a reserva dura de 30 minutos a 4 horas");
        }
        if (participantes < 1 || participantes > sala.capacidade()) {
            throw new ReservaInvalidaException("a sala %s comporta de 1 a %d pessoas".formatted(sala.id(), sala.capacidade()));
        }
        return new Reserva(UUID.randomUUID(), sala.id(), responsavel, participantes, periodo, StatusDaReserva.ATIVA);
    }

    private static boolean multiploDe15(LocalDateTime instante) {
        return instante.getMinute() % 15 == 0 && instante.getSecond() == 0 && instante.getNano() == 0;
    }

    /** Só o responsável, e só antes do início. Cancelar de novo não muda nada. */
    public void cancelar(String solicitante, LocalDateTime agora) {
        if (!responsavel.equals(solicitante)) {
            throw new OperacaoNaoPermitidaException("só quem reservou pode cancelar");
        }
        if (status == StatusDaReserva.CANCELADA) {
            return;
        }
        if (!agora.isBefore(periodo.inicio())) {
            throw new ReservaInvalidaException("a reserva já começou");
        }
        status = StatusDaReserva.CANCELADA;
    }

    public boolean conflitaCom(Reserva outra) {
        return ativa() && outra.ativa() && sala.equals(outra.sala) && periodo.sobrepoe(outra.periodo);
    }

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
