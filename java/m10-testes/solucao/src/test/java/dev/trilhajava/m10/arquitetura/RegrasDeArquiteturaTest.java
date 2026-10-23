package dev.trilhajava.m10.arquitetura;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Confere a solução de referência do exercício 10.04 contra o enunciado: cada regra PASSA no projeto limpo e
 * FALHA na violação correspondente (e só nela). Garante que o que o site mostra funciona; não faz parte do starter.
 */
@DisplayName("Exercício 10.04 — Regras de arquitetura com ArchUnit")
class RegrasDeArquiteturaTest {

    static final String EXEMPLOS = "dev.trilhajava.m10.arquitetura.exemplos";
    static final List<String> VIOLACOES = List.of("camadas", "dominio", "injecao", "transacao", "ciclo");

    static Stream<Arguments> regras() {
        return Stream.of(
                Arguments.of("camadas", (Function<String, ArchRule>) RegrasDeArquitetura::camadas, "camadas"),
                Arguments.of("dominioIndependente", (Function<String, ArchRule>) RegrasDeArquitetura::dominioIndependente, "dominio"),
                Arguments.of("semInjecaoEmCampo", (Function<String, ArchRule>) RegrasDeArquitetura::semInjecaoEmCampo, "injecao"),
                Arguments.of("transacoesForaDaApi", (Function<String, ArchRule>) RegrasDeArquitetura::transacoesForaDaApi, "transacao"),
                Arguments.of("semCiclosNoDominio", (Function<String, ArchRule>) RegrasDeArquitetura::semCiclosNoDominio, "ciclo"));
    }

    static EvaluationResult avaliar(Function<String, ArchRule> regra, String raiz) {
        JavaClasses classes = new ClassFileImporter().importPackages(raiz);
        return regra.apply(raiz).evaluate(classes);
    }

    @ParameterizedTest(name = "{0} passa no projeto limpo")
    @MethodSource("regras")
    @DisplayName("aceita o projeto que segue a arquitetura")
    void aceitaCodigoLimpo(String nome, Function<String, ArchRule> regra, String violacao) {
        EvaluationResult resultado = avaliar(regra, EXEMPLOS + ".limpo");
        assertThat(resultado.hasViolation())
                .as("%s acusou o projeto LIMPO:%n%s", nome, resultado.getFailureReport())
                .isFalse();
    }

    @ParameterizedTest(name = "{0} falha em violacao.{2}")
    @MethodSource("regras")
    @DisplayName("acusa o projeto que viola a regra")
    void acusaViolacao(String nome, Function<String, ArchRule> regra, String violacao) {
        EvaluationResult resultado = avaliar(regra, EXEMPLOS + ".violacao." + violacao);
        assertThat(resultado.hasViolation())
                .as("%s deixou passar violacao.%s", nome, violacao)
                .isTrue();
    }

    @ParameterizedTest(name = "{0} passa nas outras violações")
    @MethodSource("regras")
    @DisplayName("cada exemplo de violação quebra exatamente uma regra")
    void passaNasOutrasViolacoes(String nome, Function<String, ArchRule> regra, String violacao) {
        for (String outra : VIOLACOES) {
            if (outra.equals(violacao)) {
                continue;
            }
            EvaluationResult resultado = avaliar(regra, EXEMPLOS + ".violacao." + outra);
            assertThat(resultado.hasViolation())
                    .as("%s acusou violacao.%s, que não é a violação dela:%n%s", nome, outra, resultado.getFailureReport())
                    .isFalse();
        }
    }
}
