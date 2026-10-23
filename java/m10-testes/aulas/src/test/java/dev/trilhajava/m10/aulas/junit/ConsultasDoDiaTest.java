package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.Convenio.BASICO;
import static dev.trilhajava.m10.aulas.junit.Convenio.PARTICULAR;
import static dev.trilhajava.m10.aulas.junit.Convenio.PREMIUM;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.AMANHA;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.RELOGIO;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.amanhaAs;
import static dev.trilhajava.m10.aulas.junit.Especialidade.CARDIOLOGIA;
import static dev.trilhajava.m10.aulas.junit.Especialidade.CLINICA_GERAL;
import static dev.trilhajava.m10.aulas.junit.Especialidade.DERMATOLOGIA;
import static dev.trilhajava.m10.aulas.junit.Especialidade.PEDIATRIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Consultas do dia (AssertJ a fundo)")
class ConsultasDoDiaTest {

    Agenda agenda;

    @BeforeEach
    void montarAgenda() {
        agenda = new Agenda(RELOGIO);
        // marcadas fora de ordem de propósito
        agenda.agendar("Carla", DERMATOLOGIA, PREMIUM, amanhaAs(15, 30));    // C-1
        agenda.agendar("Ana", CARDIOLOGIA, PARTICULAR, amanhaAs(9, 0));      // C-2
        agenda.agendar("Bruno", CLINICA_GERAL, BASICO, amanhaAs(11, 0));     // C-3
        agenda.agendar("Davi", PEDIATRIA, PREMIUM, AMANHA.plusDays(1).atTime(9, 0)); // C-4, outro dia
    }

    @Test
    @DisplayName("lista só as do dia, em ordem de horário")
    void emOrdemDeHorario() {
        List<Consulta> consultas = agenda.consultasDo(AMANHA);

        assertThat(consultas)
                .extracting(Consulta::id)
                .containsExactly("C-2", "C-3", "C-1");
    }

    @Test
    @DisplayName("cada consulta tem o paciente e o horário certos")
    void pacienteEHorario() {
        assertThat(agenda.consultasDo(AMANHA))
                .extracting(Consulta::paciente, Consulta::inicio)
                .containsExactly(
                        tuple("Ana", amanhaAs(9, 0)),
                        tuple("Bruno", amanhaAs(11, 0)),
                        tuple("Carla", amanhaAs(15, 30)));
    }

    @Test
    @DisplayName("filtros e condições sobre a lista inteira")
    void filtrosECondicoes() {
        List<Consulta> consultas = agenda.consultasDo(AMANHA);

        assertThat(consultas)
                .hasSize(3)
                .allSatisfy(c -> assertThat(c.inicio().toLocalDate()).isEqualTo(AMANHA))
                .noneMatch(c -> c.paciente().equals("Davi"));
        assertThat(consultas)
                .filteredOn(c -> c.convenio() == PREMIUM)
                .singleElement()
                .extracting(Consulta::paciente)
                .isEqualTo("Carla");
    }

    @Test
    @DisplayName("o faturamento previsto do dia soma os valores com desconto")
    void faturamentoDoDia() {
        BigDecimal total = agenda.consultasDo(AMANHA).stream()
                .map(Consulta::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 320,00 + 153,00 + 159,84
        assertThat(total).as("faturamento previsto para %s", AMANHA).isEqualByComparingTo("632.84");
    }

    @Test
    @DisplayName("a consulta da Carla, campo a campo, sem parar no primeiro erro")
    void campoACampo() {
        Consulta carla = agenda.consultasDo(AMANHA).getLast();

        assertSoftly(soft -> {
            soft.assertThat(carla.id()).as("id").isEqualTo("C-1");
            soft.assertThat(carla.paciente()).as("paciente").isEqualTo("Carla");
            soft.assertThat(carla.especialidade()).as("especialidade").isEqualTo(DERMATOLOGIA);
            soft.assertThat(carla.valor()).as("valor").isEqualByComparingTo("159.84");
            soft.assertThat(carla.fim()).as("fim").isEqualTo(amanhaAs(16, 0));
        });
    }
}
