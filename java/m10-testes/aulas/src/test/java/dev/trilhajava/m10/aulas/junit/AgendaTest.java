package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.Convenio.BASICO;
import static dev.trilhajava.m10.aulas.junit.Convenio.PARTICULAR;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.AGORA;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.AMANHA;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.amanhaAs;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.hojeAs;
import static dev.trilhajava.m10.aulas.junit.Especialidade.CARDIOLOGIA;
import static dev.trilhajava.m10.aulas.junit.Especialidade.CLINICA_GERAL;
import static dev.trilhajava.m10.aulas.junit.Especialidade.DERMATOLOGIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Agenda da clínica")
class AgendaTest {

    RelogioDeTeste relogio;
    Agenda agenda;

    @BeforeEach
    void criarAgenda() {
        relogio = new RelogioDeTeste(AGORA);
        agenda = new Agenda(relogio);
    }

    @Nested
    @DisplayName("ao agendar")
    class AoAgendar {

        @Test
        @DisplayName("aplica o desconto do convênio e reserva 30 minutos")
        void consultaComConvenio() {
            Consulta consulta = agenda.agendar("Ana", DERMATOLOGIA, BASICO, amanhaAs(14, 0));

            assertThat(consulta.valor()).isEqualByComparingTo("209.02");   // 245,90 × 0,85 = 209,015
            assertThat(consulta.fim()).isEqualTo(amanhaAs(14, 30));
        }

        @Test
        @DisplayName("recusa paciente em branco (com assertThrows, do JUnit)")
        void pacienteEmBrancoComJUnit() {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> agenda.agendar("  ", CLINICA_GERAL, PARTICULAR, amanhaAs(10, 0)));

            assertEquals("paciente é obrigatório", erro.getMessage());
        }

        @Test
        @DisplayName("recusa paciente em branco (com assertThatThrownBy, do AssertJ)")
        void pacienteEmBrancoComAssertJ() {
            assertThatThrownBy(() -> agenda.agendar("  ", CLINICA_GERAL, PARTICULAR, amanhaAs(10, 0)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("paciente é obrigatório");
        }

        @Test
        @DisplayName("recusa um horário já ocupado, e a primeira consulta continua na agenda")
        void horarioOcupado() {
            agenda.agendar("Ana", CARDIOLOGIA, PARTICULAR, amanhaAs(10, 0));

            assertThatThrownBy(() -> agenda.agendar("Bruno", CLINICA_GERAL, BASICO, amanhaAs(10, 0)))
                    .isInstanceOf(HorarioIndisponivelException.class)
                    .hasMessageContaining("já ocupado")
                    .hasMessageContaining("2026-09-15T10:00");
            assertThat(agenda.consultasDo(AMANHA)).extracting(Consulta::paciente).containsExactly("Ana");
        }
    }

    @Nested
    @DisplayName("ao cancelar")
    class AoCancelar {

        Consulta amanhaCedo;

        @BeforeEach
        void marcarConsulta() {   // roda DEPOIS do criarAgenda() da classe de fora
            amanhaCedo = agenda.agendar("Ana", CARDIOLOGIA, PARTICULAR, amanhaAs(10, 0));
        }

        @Test
        @DisplayName("com 24h ou mais de antecedência, não cobra multa")
        void semMulta() {
            BigDecimal multa = agenda.cancelar(amanhaCedo.id());   // faltam 25h

            assertThat(multa).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("com menos de 24h, cobra 50% do valor")
        void comMulta() {
            Consulta hojeATarde = agenda.agendar("Bruno", CARDIOLOGIA, PARTICULAR, hojeAs(15, 0));

            assertThat(agenda.cancelar(hojeATarde.id())).isEqualByComparingTo("160.00");
        }

        @Test
        @DisplayName("tira a consulta da agenda e libera o horário")
        void liberaOHorario() {
            agenda.cancelar(amanhaCedo.id());

            assertThat(agenda.consultasDo(AMANHA)).isEmpty();
            assertThat(agenda.agendar("Bruno", CLINICA_GERAL, PARTICULAR, amanhaAs(10, 0)).id())
                    .isEqualTo("C-2");
        }

        @Test
        @DisplayName("depois que a consulta começou, não cancela mais")
        void consultaJaComecou() {
            relogio.avancar(Duration.ofHours(25));   // agora são 10:00 de amanhã

            assertThatThrownBy(() -> agenda.cancelar(amanhaCedo.id()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("a consulta C-1 já começou");
        }

        @Test
        @DisplayName("recusa uma consulta que não existe")
        void consultaInexistente() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> agenda.cancelar("C-99"))
                    .withMessage("consulta inexistente: C-99");
        }
    }

    @Nested
    @DisplayName("sem nenhuma consulta marcada")
    class AgendaVazia {

        @Test
        @DisplayName("devolve uma lista vazia para qualquer dia")
        void listaVazia() {
            assertThat(agenda.consultasDo(LocalDate.of(2026, 12, 1))).isEmpty();
        }
    }
}
