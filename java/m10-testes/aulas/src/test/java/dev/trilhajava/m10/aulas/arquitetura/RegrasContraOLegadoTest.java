package dev.trilhajava.m10.aulas.arquitetura;

import static dev.trilhajava.m10.aulas.arquitetura.ArquiteturaDoConsultorioTest.CAMADAS;
import static dev.trilhajava.m10.aulas.arquitetura.ArquiteturaDoConsultorioTest.CONTROLLERS;
import static dev.trilhajava.m10.aulas.arquitetura.ArquiteturaDoConsultorioTest.DOMINIO_SEM_CICLOS;
import static dev.trilhajava.m10.aulas.arquitetura.ArquiteturaDoConsultorioTest.DOMINIO_SEM_SPRING;
import static dev.trilhajava.m10.aulas.arquitetura.ArquiteturaDoConsultorioTest.REPOSITORIOS;
import static dev.trilhajava.m10.aulas.arquitetura.ArquiteturaDoConsultorioTest.TEMPO_SO_PELO_RELOGIO;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * Uma regra que nunca falha não prova nada. Aqui cada regra do consultório é aplicada ao
 * consultório legado, que viola todas, e o teste exige que ela acuse a violação certa.
 */
class RegrasContraOLegadoTest {

    static final JavaClasses LEGADO = new ClassFileImporter()
            .importPackages("dev.trilhajava.m10.aulas.arquitetura.legado");

    @Test
    void camadasAcusaOControllerQueLeDaPersistencia() {
        assertThatThrownBy(() -> CAMADAS.check(LEGADO))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("was violated (3 times)")
                .hasMessageContaining("ConsultaController.listar()> calls method <")
                .hasMessageContaining("AgendaJdbc.todas()");
    }

    @Test
    void dominioSemSpringAcusaOAssertDoSpring() {
        assertThatThrownBy(() -> DOMINIO_SEM_SPRING.check(LEGADO))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("org.springframework.util.Assert.notNull");
    }

    @Test
    void tempoSoPeloRelogioAcusaONowSemClock() {
        assertThatThrownBy(() -> TEMPO_SO_PELO_RELOGIO.check(LEGADO))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("calls method <java.time.LocalDateTime.now()> in (AgendamentoDeConsultas.java:20)");
    }

    @Test
    void semCiclosAcusaAgendaEPaciente() {
        assertThatThrownBy(() -> DOMINIO_SEM_CICLOS.check(LEGADO))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Cycle detected: Slice agenda")
                .hasMessageContaining("Slice paciente");
    }

    @Test
    void controllersAcusaONomeFora() {
        assertThatThrownBy(() -> CONTROLLERS.check(LEGADO))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("PacienteRecurso> does not have simple name ending with 'Controller'");
    }

    @Test
    void repositoriosAcusaAClasseSemAnotacao() {
        assertThatThrownBy(() -> REPOSITORIOS.check(LEGADO))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AgendaJdbc> is not annotated with @Repository");
    }
}
