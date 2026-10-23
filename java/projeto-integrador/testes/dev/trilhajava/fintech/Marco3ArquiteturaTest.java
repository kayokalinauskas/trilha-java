package dev.trilhajava.fintech;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fintech Core — Marco 3: as regras de arquitetura do projeto. Valem para o SEU código (não para os testes).
 * Rode: mvn test -Dtest=Marco3ArquiteturaTest
 */
@AnalyzeClasses(packages = "dev.trilhajava.fintech", importOptions = ImportOption.DoNotIncludeTests.class)
class Marco3ArquiteturaTest {

    @ArchTest
    static final ArchRule camadas = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("API").definedBy("dev.trilhajava.fintech.api..")
            .layer("Aplicação").definedBy("dev.trilhajava.fintech.aplicacao..")
            .layer("Domínio").definedBy("dev.trilhajava.fintech.dominio..")
            .layer("Infra").definedBy("dev.trilhajava.fintech.infra..")
            .whereLayer("API").mayNotBeAccessedByAnyLayer()
            .whereLayer("Aplicação").mayOnlyBeAccessedByLayers("API", "Infra")
            .whereLayer("Infra").mayNotBeAccessedByAnyLayer()
            .whereLayer("Domínio").mayOnlyBeAccessedByLayers("API", "Aplicação", "Infra");

    @ArchTest
    static final ArchRule dominio_nao_depende_do_spring = noClasses()
            .that().resideInAPackage("dev.trilhajava.fintech.dominio..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule sem_injecao_em_campo = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static final ArchRule controllers_so_na_api = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("dev.trilhajava.fintech.api..");

    // A anotação é referenciada pelo nome: o starter (marco 1) ainda não tem spring-tx no classpath.
    @ArchTest
    static final ArchRule transacoes_fora_da_api = noMethods()
            .that().areDeclaredInClassesThat().resideInAPackage("dev.trilhajava.fintech.api..")
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional");
}
