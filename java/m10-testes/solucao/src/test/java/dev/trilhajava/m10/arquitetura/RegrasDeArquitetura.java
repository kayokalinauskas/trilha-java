package dev.trilhajava.m10.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * As decisões de arquitetura do time, escritas como testes: quem quebrar uma delas quebra o build,
 * com uma mensagem que explica o porquê. Documentação que não envelhece.
 */
public final class RegrasDeArquitetura {

    private RegrasDeArquitetura() {
    }

    public static ArchRule camadas(String raiz) {
        return layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("API").definedBy(raiz + ".api..")
                .layer("Aplicação").definedBy(raiz + ".aplicacao..")
                .layer("Domínio").definedBy(raiz + ".dominio..")
                .layer("Infra").definedBy(raiz + ".infra..")
                .whereLayer("API").mayNotBeAccessedByAnyLayer()
                .whereLayer("Aplicação").mayOnlyBeAccessedByLayers("API", "Infra")
                .whereLayer("Infra").mayNotBeAccessedByAnyLayer()
                .whereLayer("Domínio").mayOnlyBeAccessedByLayers("API", "Aplicação", "Infra")
                .because("as dependências apontam para o domínio; a infra é plugada por interfaces");
    }

    public static ArchRule dominioIndependente(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".dominio..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", raiz + ".api..", raiz + ".aplicacao..", raiz + ".infra..")
                .because("o domínio é o núcleo: regras de negócio não dependem de framework nem de detalhes técnicos");
    }

    public static ArchRule semInjecaoEmCampo(String raiz) {
        return noFields().that().areDeclaredInClassesThat().resideInAPackage(raiz + "..")
                .should().beAnnotatedWith(Autowired.class)
                .because("injeção por construtor deixa as dependências explícitas, os campos final e o teste sem Spring");
    }

    public static ArchRule transacoesForaDaApi(String raiz) {
        ArchRule classes = noClasses().that().resideInAPackage(raiz + ".api..")
                .should().beAnnotatedWith(Transactional.class);
        ArchRule metodos = noMethods().that().areDeclaredInClassesThat().resideInAPackage(raiz + ".api..")
                .should().beAnnotatedWith(Transactional.class);
        return CompositeArchRule.of(classes).and(metodos)
                .because("a fronteira da transação é o caso de uso (aplicacao), não o controller");
    }

    public static ArchRule semCiclosNoDominio(String raiz) {
        return slices().matching(raiz + ".dominio.(*)..")
                .should().beFreeOfCycles()
                .because("ciclos entre módulos do domínio impedem entender, testar e separar cada um");
    }
}
