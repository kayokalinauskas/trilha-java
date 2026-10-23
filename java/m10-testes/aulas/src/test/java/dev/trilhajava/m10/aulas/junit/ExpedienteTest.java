package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.AMANHA;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.RELOGIO;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.amanhaAs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Expediente e dados obrigatórios")
class ExpedienteTest {

    private final Agenda agenda = new Agenda(RELOGIO);

    @ParameterizedTest(name = "aceita amanhã às {0}")
    @ValueSource(strings = {"08:00", "08:30", "12:00", "17:30"})
    void aceitaDentroDoExpediente(LocalTime hora) {
        Consulta consulta = agenda.agendar("Ana", Especialidade.PEDIATRIA, Convenio.BASICO, AMANHA.atTime(hora));

        assertThat(consulta.inicio().toLocalTime()).isEqualTo(hora);
    }

    @ParameterizedTest(name = "recusa amanhã às {0}")
    @ValueSource(strings = {"07:30", "18:00", "12:15", "10:00:30"})
    void recusaForaDoExpediente(LocalTime hora) {
        assertThatThrownBy(() -> agenda.agendar("Ana", Especialidade.PEDIATRIA, Convenio.BASICO, AMANHA.atTime(hora)))
                .isInstanceOf(HorarioIndisponivelException.class);
    }

    @ParameterizedTest(name = "recusa {0} (fim de semana)")
    @ValueSource(strings = {"2026-09-19", "2026-09-20"})
    void recusaFimDeSemana(LocalDate dia) {
        assertThatThrownBy(() -> agenda.agendar("Ana", Especialidade.PEDIATRIA, Convenio.BASICO, dia.atTime(10, 0)))
                .isInstanceOf(HorarioIndisponivelException.class)
                .hasMessageStartingWith("a clínica não abre aos sábados e domingos");
    }

    @ParameterizedTest(name = "recusa paciente [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void recusaPacienteSemNome(String paciente) {
        assertThatThrownBy(() -> agenda.agendar(paciente, Especialidade.PEDIATRIA, Convenio.BASICO, amanhaAs(10, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("paciente é obrigatório");
    }
}
