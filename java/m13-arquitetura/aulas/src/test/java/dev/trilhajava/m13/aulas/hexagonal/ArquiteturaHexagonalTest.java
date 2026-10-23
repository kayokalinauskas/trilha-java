package dev.trilhajava.m13.aulas.hexagonal;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

/** A arquitetura como teste: se alguém criar um atalho, o build quebra. */
@AnalyzeClasses(packages = "dev.trilhajava.m13.aulas.hexagonal", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaHexagonalTest {

    @ArchTest
    static final ArchRule dependenciasApontamParaDentro = CompositeArchRule
            .of(onionArchitecture()
                    .domainModels("..hexagonal.dominio..")
                    .applicationServices("..hexagonal.aplicacao..")
                    .adapter("web", "..hexagonal.adaptador.web..")
                    .adapter("persistencia", "..hexagonal.adaptador.persistencia..")
                    .adapter("mensageria", "..hexagonal.adaptador.mensageria..")
                    .withOptionalLayers(true)
                    // a montagem e a classe da aplicação ficam no pacote raiz e podem ver tudo
                    .ignoreDependency(resideInAPackage("dev.trilhajava.m13.aulas.hexagonal"), alwaysTrue()))
            .and(noClasses().that().resideInAnyPackage("..hexagonal.dominio..", "..hexagonal.aplicacao..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta..", "java.sql..")
                    .as("o núcleo não depende de framework, de HTTP nem de JDBC"));
}
