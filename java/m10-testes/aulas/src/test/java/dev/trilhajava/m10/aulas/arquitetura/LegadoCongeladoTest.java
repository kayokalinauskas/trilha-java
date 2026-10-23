package dev.trilhajava.m10.aulas.arquitetura;

import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * O consultório legado já viola as regras. Congeladas, elas aceitam as violações antigas
 * (gravadas em src/test/resources/archunit_store) e acusam qualquer violação nova.
 */
@AnalyzeClasses(
        packages = "dev.trilhajava.m10.aulas.arquitetura.legado",
        importOptions = ImportOption.DoNotIncludeTests.class)
class LegadoCongeladoTest {

    @ArchTest
    static final ArchRule CAMADAS = freeze(ArquiteturaDoConsultorioTest.CAMADAS);

    @ArchTest
    static final ArchRule TEMPO_SO_PELO_RELOGIO = freeze(ArquiteturaDoConsultorioTest.TEMPO_SO_PELO_RELOGIO);
}
