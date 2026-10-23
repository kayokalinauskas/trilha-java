package dev.trilhajava.m13.aulas.decisoes;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/** A decisão do ADR-0007 virou teste: se alguém furar a abstração, o build quebra. */
class FitnessFunctionsTest {

    static final ArchRule SO_A_ABSTRACAO_E_PUBLICA = noClasses()
            .that().resideOutsideOfPackage("..decisoes.frete..")
            .should().dependOnClassesThat().resideInAnyPackage("..frete.legado..", "..frete.novo..")
            .because("o ADR-0007 decidiu que o resto do sistema só conhece CalculadoraDeFrete");

    @Test
    void oCodigoDeProducaoRespeitaADecisao() {
        var producao = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.trilhajava.m13.aulas.decisoes");

        SO_A_ABSTRACAO_E_PUBLICA.check(producao);
    }

    @Test
    void oPullRequestQueFuraAAbstracaoQuebraOBuild() {
        var comOPullRequest = new ClassFileImporter().importPackages("dev.trilhajava.m13.aulas.decisoes");

        assertThatThrownBy(() -> SO_A_ABSTRACAO_E_PUBLICA.check(comOPullRequest))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("because o ADR-0007 decidiu")
                .hasMessageContaining("RelatorioDeFreteApressado.freteMedioParaSp()");
    }
}
