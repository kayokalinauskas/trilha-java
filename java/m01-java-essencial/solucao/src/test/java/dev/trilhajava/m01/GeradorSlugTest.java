package dev.trilhajava.m01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Confere a solução de referência do exercício 01.04 contra os exemplos do enunciado (garante que o que o
 * site mostra funciona). Não faz parte do starter.
 */
@DisplayName("Exercício 01.04 — Gerador de slugs para URLs")
class GeradorSlugTest {

    @Nested
    @DisplayName("Tabela de requisitos — exemplos do enunciado")
    class Exemplos {

        @ParameterizedTest(name = "\"{0}\" vira \"{1}\"")
        @CsvSource(delimiter = '|', textBlock = """
                Olá Mundo                    | ola-mundo
                Java 25: o que mudou?        | java-25-o-que-mudou
                Ação, Reação & Opção         | acao-reacao-opcao
                ---Já    tem   hífens---     | ja-tem-hifens
                Café_com_pão                 | cafe-com-pao
                ÇÃO Ñandú Über               | cao-nandu-uber
                """)
        void tabela(String titulo, String esperado) {
            assertThat(GeradorSlug.gerar(titulo))
                    .as("slug de \"%s\" (veja a tabela de requisitos)", titulo)
                    .isEqualTo(esperado);
        }
    }

    @Nested
    @DisplayName("Regra 1 — tudo em minúsculas")
    class Regra1 {

        @Test
        @DisplayName("\"JAVA Moderno\" vira \"java-moderno\"")
        void minusculas() {
            assertThat(GeradorSlug.gerar("JAVA Moderno"))
                    .as("letras maiúsculas precisam virar minúsculas, não ser descartadas")
                    .isEqualTo("java-moderno");
        }
    }

    @Nested
    @DisplayName("Regra 2 — acentos removidos, a letra fica")
    class Regra2 {

        @ParameterizedTest(name = "\"{0}\" vira \"{1}\"")
        @CsvSource(delimiter = '|', textBlock = """
                Ação, Reação & Opção | acao-reacao-opcao
                ÇÃO Ñandú Über       | cao-nandu-uber
                """)
        void acentos(String titulo, String esperado) {
            assertThat(GeradorSlug.gerar(titulo))
                    .as("'ç' vira 'c', 'ã' vira 'a', 'ñ' vira 'n'... a letra continua, só o acento sai")
                    .isEqualTo(esperado);
        }
    }

    @Nested
    @DisplayName("Regra 3 — só a-z e 0-9; cada sequência de outros caracteres vira um único hífen")
    class Regra3 {

        @ParameterizedTest(name = "\"{0}\" vira \"{1}\"")
        @CsvSource(delimiter = '|', textBlock = """
                Java 25: o que mudou? | java-25-o-que-mudou
                Café_com_pão          | cafe-com-pao
                """)
        void separadores(String titulo, String esperado) {
            assertThat(GeradorSlug.gerar(titulo))
                    .as("\": \", \"_\" e espaços são 'outros caracteres': cada sequência deles vira UM hífen")
                    .isEqualTo(esperado);
        }
    }

    @Nested
    @DisplayName("Regra 4 — sem hífen no começo nem no fim")
    class Regra4 {

        @Test
        @DisplayName("\"---Já    tem   hífens---\" vira \"ja-tem-hifens\"")
        void hifensNasPontas() {
            assertThat(GeradorSlug.gerar("---Já    tem   hífens---"))
                    .as("os hífens do começo e do fim do título não podem sobrar nas pontas do slug")
                    .isEqualTo("ja-tem-hifens");
        }

        @Test
        @DisplayName("\"  Olá Mundo!  \" vira \"ola-mundo\"")
        void espacosEPontuacaoNasPontas() {
            assertThat(GeradorSlug.gerar("  Olá Mundo!  "))
                    .as("espaços e '!' nas pontas também são 'outros caracteres': não geram hífen nas pontas")
                    .isEqualTo("ola-mundo");
        }
    }

    @Nested
    @DisplayName("Regra 5 — no máximo 60 caracteres, sem cortar palavra no meio")
    class Regra5 {

        @Test
        @DisplayName("título longo: fica só com as palavras inteiras que cabem em 60")
        void cortaNoHifen() {
            String titulo = "Como resolver o problema de N mais um no Hibernate sem perder a sanidade mental";
            assertThat(GeradorSlug.gerar(titulo))
                    .as("o slug completo passa de 60; \"perder\" não cabe inteira, então o corte volta até o hífen"
                            + " anterior (e esse hífen também sai)")
                    .isEqualTo("como-resolver-o-problema-de-n-mais-um-no-hibernate-sem");
        }

        @Test
        @DisplayName("palavra que termina exatamente no 60º caractere cabe inteira")
        void palavraTerminaNoLimite() {
            String titulo = "Um guia completo de testes automatizados com JUnit e Mockito para quem está começando";
            assertThat(GeradorSlug.gerar(titulo))
                    .as("\"mockito\" termina exatamente no caractere 60, então não há o que cortar")
                    .isEqualTo("um-guia-completo-de-testes-automatizados-com-junit-e-mockito");
        }

        @Test
        @DisplayName("palavra única com mais de 60 caracteres é cortada em 60")
        void palavraGigante() {
            assertThat(GeradorSlug.gerar("a".repeat(80)))
                    .as("sem nenhum hífen para recuar, o jeito é cortar exatamente em 60")
                    .isEqualTo("a".repeat(60));
        }
    }

    @Nested
    @DisplayName("Regra 6 — sem nenhuma letra ou dígito: IllegalArgumentException")
    class Regra6 {

        @ParameterizedTest(name = "[{0}] não gera slug")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "!!!", "—"})
        void semConteudo(String titulo) {
            assertThatThrownBy(() -> GeradorSlug.gerar(titulo))
                    .as("um título sem letras nem dígitos não tem slug possível: a regra 6 pede"
                            + " IllegalArgumentException, e não um slug vazio")
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
