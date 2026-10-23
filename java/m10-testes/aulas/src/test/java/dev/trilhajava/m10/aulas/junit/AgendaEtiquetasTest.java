package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.RELOGIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("Agenda: testes com etiqueta e desligados")
class AgendaEtiquetasTest {

    @Test
    @Tag("carga")
    @DisplayName("aguenta um ano inteiro de agenda lotada, sem perder a ordem")
    void anoInteiroLotado() {
        Agenda agenda = new Agenda(RELOGIO);
        LocalDate primeiroDia = LocalDate.of(2026, 9, 15);
        LocalDate ultimoDia = primeiroDia.plusYears(1);

        int marcadas = 0;
        for (LocalDate dia = primeiroDia; dia.isBefore(ultimoDia); dia = dia.plusDays(1)) {
            if (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            for (LocalDateTime horario = dia.atTime(8, 0); !horario.isAfter(dia.atTime(17, 30));
                    horario = horario.plusMinutes(30)) {
                agenda.agendar("Paciente " + marcadas, Especialidade.CLINICA_GERAL, Convenio.PARTICULAR, horario);
                marcadas++;
            }
        }

        assertThat(marcadas).isEqualTo(261 * 20);   // 261 dias úteis × 20 horários
        assertThat(agenda.consultasDo(ultimoDia.minusDays(1)))
                .hasSize(20)
                .isSortedAccordingTo((a, b) -> a.inicio().compareTo(b.inicio()));
    }

    @Test
    @Disabled("Feriados ainda não implementados: aguardando a tabela oficial (issue #42)")
    @DisplayName("não agenda em feriado nacional (Finados, 02/11)")
    void feriadoNacional() {
        Agenda agenda = new Agenda(RELOGIO);

        assertThatThrownBy(() -> agenda.agendar("Ana", Especialidade.CLINICA_GERAL, Convenio.PARTICULAR,
                LocalDate.of(2026, 11, 2).atTime(10, 0)))
                .isInstanceOf(HorarioIndisponivelException.class);
    }
}
