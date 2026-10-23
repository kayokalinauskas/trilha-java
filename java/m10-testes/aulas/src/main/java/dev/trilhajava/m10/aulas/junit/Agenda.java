package dev.trilhajava.m10.aulas.junit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A agenda de uma clínica. Regras:
 *
 * <ol>
 *   <li>Paciente é obrigatório: em branco ou nulo, {@link IllegalArgumentException}.</li>
 *   <li>Atendimento de segunda a sexta. Consultas de 30 minutos, começando na hora cheia ou
 *       na meia hora; a primeira às 08:00 e a última às 17:30.</li>
 *   <li>Antecedência mínima de 2 horas (exatamente 2 horas vale).</li>
 *   <li>Um horário comporta uma só consulta.</li>
 *   <li>Valor: o da especialidade com o desconto do convênio, em centavos (HALF_UP).</li>
 *   <li>Cancelar com 24 horas ou mais de antecedência: sem multa. Com menos: multa de 50%
 *       do valor. Depois do início: {@link IllegalStateException}.</li>
 * </ol>
 *
 * O "agora" vem sempre do {@link Clock} recebido no construtor.
 */
public class Agenda {

    static final Duration DURACAO = Duration.ofMinutes(30);
    static final Duration ANTECEDENCIA_MINIMA = Duration.ofHours(2);
    static final Duration ANTECEDENCIA_SEM_MULTA = Duration.ofHours(24);
    static final LocalTime PRIMEIRO_HORARIO = LocalTime.of(8, 0);
    static final LocalTime ULTIMO_HORARIO = LocalTime.of(17, 30);
    private static final BigDecimal PERCENTUAL_DA_MULTA = new BigDecimal("0.50");

    private final Clock clock;
    private final Map<String, Consulta> consultas = new LinkedHashMap<>();
    private int sequencia;

    public Agenda(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Consulta agendar(String paciente, Especialidade especialidade, Convenio convenio,
            LocalDateTime inicio) {
        if (paciente == null || paciente.isBlank()) {
            throw new IllegalArgumentException("paciente é obrigatório");
        }
        Objects.requireNonNull(especialidade, "especialidade");
        Objects.requireNonNull(convenio, "convenio");
        validarExpediente(inicio);

        LocalDateTime agora = LocalDateTime.now(clock);
        if (inicio.isBefore(agora.plus(ANTECEDENCIA_MINIMA))) {
            throw new HorarioIndisponivelException("agende com pelo menos 2h de antecedência: " + inicio);
        }
        if (ocupado(inicio)) {
            throw new HorarioIndisponivelException("horário já ocupado: " + inicio);
        }

        BigDecimal valor = convenio.aplicarDesconto(especialidade.valor());
        var consulta = new Consulta("C-" + (++sequencia), paciente, especialidade, convenio, inicio, valor);
        consultas.put(consulta.id(), consulta);
        return consulta;
    }

    /** Cancela a consulta e devolve a multa cobrada (0.00 quando não há multa). */
    public BigDecimal cancelar(String id) {
        Consulta consulta = consultas.get(id);
        if (consulta == null) {
            throw new IllegalArgumentException("consulta inexistente: " + id);
        }
        LocalDateTime agora = LocalDateTime.now(clock);
        if (!agora.isBefore(consulta.inicio())) {
            throw new IllegalStateException("a consulta " + id + " já começou");
        }

        Duration antecedencia = Duration.between(agora, consulta.inicio());
        BigDecimal multa = antecedencia.compareTo(ANTECEDENCIA_SEM_MULTA) >= 0
                ? BigDecimal.ZERO.setScale(2)
                : consulta.valor().multiply(PERCENTUAL_DA_MULTA).setScale(2, RoundingMode.HALF_UP);
        consultas.remove(id);
        return multa;
    }

    /** As consultas de um dia, em ordem de horário. */
    public List<Consulta> consultasDo(LocalDate dia) {
        return consultas.values().stream()
                .filter(c -> c.inicio().toLocalDate().equals(dia))
                .sorted(Comparator.comparing(Consulta::inicio))
                .toList();
    }

    private void validarExpediente(LocalDateTime inicio) {
        Objects.requireNonNull(inicio, "inicio");
        DayOfWeek dia = inicio.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            throw new HorarioIndisponivelException("a clínica não abre aos sábados e domingos: " + inicio);
        }
        LocalTime hora = inicio.toLocalTime();
        if (hora.isBefore(PRIMEIRO_HORARIO) || hora.isAfter(ULTIMO_HORARIO)) {
            throw new HorarioIndisponivelException("fora do horário de atendimento (08:00 às 18:00): " + inicio);
        }
        if (hora.getMinute() % 30 != 0 || hora.getSecond() != 0 || hora.getNano() != 0) {
            throw new HorarioIndisponivelException("consultas começam na hora cheia ou na meia hora: " + inicio);
        }
    }

    private boolean ocupado(LocalDateTime inicio) {
        return consultas.values().stream().anyMatch(c -> c.inicio().equals(inicio));
    }
}
