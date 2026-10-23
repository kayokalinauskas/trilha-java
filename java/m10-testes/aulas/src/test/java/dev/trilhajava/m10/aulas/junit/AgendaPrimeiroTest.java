package dev.trilhajava.m10.aulas.junit;

import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.RELOGIO;
import static dev.trilhajava.m10.aulas.junit.DadosDaAgenda.amanhaAs;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AgendaPrimeiroTest {

    @Test
    void agendaUmaConsultaParticular() {
        // Arrange
        Agenda agenda = new Agenda(RELOGIO);

        // Act
        Consulta consulta = agenda.agendar("Ana", Especialidade.CARDIOLOGIA, Convenio.PARTICULAR,
                amanhaAs(10, 0));

        // Assert: assertEquals(ESPERADO, OBTIDO)
        assertEquals("C-1", consulta.id());
        assertEquals(new BigDecimal("320.00"), consulta.valor());
        assertEquals(amanhaAs(10, 30), consulta.fim());
    }
}
