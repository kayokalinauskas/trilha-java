package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.RELOGIO;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.amanhaAs;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("Preço da consulta")
class PrecoDaConsultaTest {

    private final Agenda agenda = new Agenda(RELOGIO);

    @ParameterizedTest(name = "{0} com {1}: R$ {2}", quoteTextArguments = false)
    @CsvSource(textBlock = """
            # especialidade, convênio,   valor esperado (calculado à mão)
            CLINICA_GERAL,   PARTICULAR, 180.00
            CLINICA_GERAL,   PREMIUM,    117.00
            CARDIOLOGIA,     BASICO,     272.00
            DERMATOLOGIA,    BASICO,     209.02
            DERMATOLOGIA,    PREMIUM,    159.84
            PEDIATRIA,       PREMIUM,    136.50
            """)
    void valorComDesconto(Especialidade especialidade, Convenio convenio, BigDecimal esperado) {
        Consulta consulta = agenda.agendar("Ana", especialidade, convenio, amanhaAs(10, 0));

        assertThat(consulta.valor()).isEqualByComparingTo(esperado);
    }

    @ParameterizedTest(name = "{0}: particular paga o valor cheio")
    @EnumSource(Especialidade.class)
    void particularPagaOValorCheio(Especialidade especialidade) {
        Consulta consulta = agenda.agendar("Ana", especialidade, Convenio.PARTICULAR, amanhaAs(10, 0));

        assertThat(consulta.valor()).isEqualByComparingTo(especialidade.valor());
    }

    @ParameterizedTest(name = "{0} sempre dá desconto")
    @EnumSource(value = Convenio.class, names = "PARTICULAR", mode = EnumSource.Mode.EXCLUDE)
    void convenioSempreDaDesconto(Convenio convenio) {
        Consulta consulta = agenda.agendar("Ana", Especialidade.CARDIOLOGIA, convenio, amanhaAs(10, 0));

        assertThat(consulta.valor()).isLessThan(Especialidade.CARDIOLOGIA.valor());
    }
}
