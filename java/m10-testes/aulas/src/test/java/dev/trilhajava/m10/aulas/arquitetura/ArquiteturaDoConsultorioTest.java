package dev.trilhajava.m10.aulas.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

/** As decisões de arquitetura do consultório, escritas como testes (aula 10.06). */
@AnalyzeClasses(
        packages = "dev.trilhajava.m10.aulas.arquitetura.consultorio",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaDoConsultorioTest {

    // 1. Camadas: quem pode acessar quem
    @ArchTest
    static final ArchRule CAMADAS = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Web").definedBy("..consultorio.web..")
            .layer("Aplicação").definedBy("..consultorio.aplicacao..")
            .layer("Domínio").definedBy("..consultorio.dominio..")
            .layer("Persistência").definedBy("..consultorio.persistencia..")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Aplicação").mayOnlyBeAccessedByLayers("Web")
            .whereLayer("Persistência").mayNotBeAccessedByAnyLayer()
            .because("a web fala com casos de uso, e a persistência só é alcançada pelas interfaces do domínio");

    // 2. Dependências proibidas
    @ArchTest
    static final ArchRule DOMINIO_SEM_SPRING = noClasses()
            .that().resideInAPackage("..consultorio.dominio..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .because("as regras de negócio não podem depender de framework");

    @ArchTest
    static final ArchRule TEMPO_SO_PELO_RELOGIO = noClasses()
            .should().callMethod(LocalDate.class, "now")
            .orShould().callMethod(LocalDateTime.class, "now")
            .orShould().callMethod(Instant.class, "now")
            .because("o tempo é uma dependência: use now(clock) com um Clock injetado");

    // 3. Ciclos entre os módulos do domínio
    @ArchTest
    static final ArchRule DOMINIO_SEM_CICLOS = slices()
            .matching("..consultorio.dominio.(*)..")
            .should().beFreeOfCycles()
            .because("módulos do domínio em ciclo não podem ser entendidos nem separados um sem o outro");

    // 4. Convenções de nome e de anotação
    @ArchTest
    static final ArchRule CONTROLLERS = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .andShould().resideInAPackage("..consultorio.web..")
            .because("quem procura um endpoint procura por *Controller no pacote web");

    @ArchTest
    static final ArchRule REPOSITORIOS = classes()
            .that().resideInAPackage("..consultorio.persistencia..")
            .and().areNotInterfaces()
            .should().beAnnotatedWith(Repository.class)
            .because("@Repository traduz as exceções do banco para a hierarquia do Spring");
}
