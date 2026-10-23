package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.HOJE;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.relogioEm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Antecedência mínima de 2 horas")
class AntecedenciaMinimaTest {

    private Agenda agendaAs(LocalTime agora) {
        return new Agenda(relogioEm(HOJE.atTime(agora)));
    }

    @ParameterizedTest(name = "às {0}, aceita a consulta das {1}")
    @CsvSource({
        "09:00, 11:00",   // exatamente 2h: o limite, aceito
        "08:59, 11:00",   // 2h01
        "09:00, 17:30",   // bem longe
    })
    void aceita(LocalTime agora, LocalTime inicio) {
        Consulta consulta = agendaAs(agora).agendar("Ana", Especialidade.PEDIATRIA, Convenio.BASICO, HOJE.atTime(inicio));

        assertThat(consulta.inicio()).isEqualTo(HOJE.atTime(inicio));
    }

    @ParameterizedTest(name = "às {0}, recusa a consulta das {1}")
    @CsvSource({
        "09:01, 11:00",   // 1h59: logo abaixo do limite
        "09:00, 10:30",   // 1h30
        "09:00, 08:30",   // já passou
    })
    void recusa(LocalTime agora, LocalTime inicio) {
        Agenda agenda = agendaAs(agora);

        assertThatThrownBy(() -> agenda.agendar("Ana", Especialidade.PEDIATRIA, Convenio.BASICO, HOJE.atTime(inicio)))
                .isInstanceOf(HorarioIndisponivelException.class)
                .hasMessageContaining("2h de antecedência");
    }
}
