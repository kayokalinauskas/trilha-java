package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.AGORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Multa de cancelamento")
class MultaDeCancelamentoTest {

    /** Quarta-feira, 16/09, 10:00: 49 horas depois de AGORA. */
    static final LocalDateTime INICIO = LocalDateTime.of(2026, 9, 16, 10, 0);

    static Stream<Arguments> antecedencias() {
        return Stream.of(
                argumentSet("bem antes (49h): sem multa",     Duration.ofHours(49),                 "0.00"),
                argumentSet("24h exatas: sem multa (limite)", Duration.ofHours(24),                 "0.00"),
                argumentSet("23h59: com multa (logo abaixo)", Duration.ofHours(23).plusMinutes(59), "160.00"),
                argumentSet("1 minuto antes: com multa",      Duration.ofMinutes(1),                "160.00"));
    }

    @ParameterizedTest
    @MethodSource("antecedencias")
    void multaPelaAntecedencia(Duration antecedencia, BigDecimal multaEsperada) {
        var relogio = new RelogioDeTeste(AGORA);
        var agenda = new Agenda(relogio);
        Consulta consulta = agenda.agendar("Ana", Especialidade.CARDIOLOGIA, Convenio.PARTICULAR, INICIO);

        relogio.avancar(Duration.between(AGORA, INICIO).minus(antecedencia));   // faltam `antecedencia`

        assertThat(agenda.cancelar(consulta.id())).isEqualByComparingTo(multaEsperada);
    }
}
