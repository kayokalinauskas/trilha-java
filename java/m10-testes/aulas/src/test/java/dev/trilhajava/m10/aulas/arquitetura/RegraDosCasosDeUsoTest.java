package dev.trilhajava.m10.aulas.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

/** Solução do checkpoint da parte 2 da aula 10.06. */
class RegraDosCasosDeUsoTest {

    static final ArchRule CASOS_DE_USO = classes()
            .that().resideInAPackage("..consultorio.aplicacao..")
            .should().beAnnotatedWith(Service.class)
            .andShould().haveSimpleNameNotEndingWith("Service")
            .because("casos de uso são componentes do Spring com nome de negócio, não XxxService");

    static final JavaClasses LIMPO = new ClassFileImporter()
            .importPackages("dev.trilhajava.m10.aulas.arquitetura.consultorio");

    static final JavaClasses CHECKPOINT = new ClassFileImporter()
            .importPackages("dev.trilhajava.m10.aulas.arquitetura.checkpoint");

    @Test
    void oConsultorioLimpoSegueARegra() {
        assertThatCode(() -> CASOS_DE_USO.check(LIMPO)).doesNotThrowAnyException();
    }

    @Test
    void aRegraAcusaOsDoisProblemasDoConsultaService() {
        assertThatThrownBy(() -> CASOS_DE_USO.check(CHECKPOINT))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("was violated (2 times)")
                .hasMessageContaining("ConsultaService> is not annotated with @Service")
                .hasMessageContaining("ConsultaService> has simple name ending with 'Service'");
    }
}
