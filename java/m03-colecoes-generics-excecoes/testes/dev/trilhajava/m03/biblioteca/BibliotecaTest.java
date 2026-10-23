package dev.trilhajava.m03.biblioteca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Exercício 03.03 — Exceções no sistema de empréstimos da biblioteca")
class BibliotecaTest {

    private static final LocalDate DIA_1 = LocalDate.of(2026, 3, 2);
    private static final Livro JAVA_EFETIVO = new Livro("978-8550804620", "Java Efetivo");
    private static final Livro CODIGO_LIMPO = new Livro("978-8576082675", "Código Limpo");

    private Biblioteca biblioteca;

    @BeforeEach
    void setUp() {
        biblioteca = new Biblioteca();
        biblioteca.cadastrar(JAVA_EFETIVO, 1);
        biblioteca.cadastrar(CODIGO_LIMPO, 5);
    }

    @Nested
    @DisplayName("empréstimo")
    class Emprestar {
        @Test
        @DisplayName("empresta, reduz disponíveis e define prazo de 14 dias")
        void empresta() {
            Emprestimo e = biblioteca.emprestar(JAVA_EFETIVO.isbn(), "ana", DIA_1);
            assertThat(e.dataPrevista()).isEqualTo(DIA_1.plusDays(14));
            assertThat(biblioteca.disponiveis(JAVA_EFETIVO.isbn())).isZero();
        }

        @Test
        @DisplayName("livro inexistente: LivroNaoEncontradoException com código de erro")
        void inexistente() {
            assertThatThrownBy(() -> biblioteca.emprestar("000", "ana", DIA_1))
                    .isInstanceOf(LivroNaoEncontradoException.class)
                    .isInstanceOf(BibliotecaException.class)
                    .hasMessageContaining("000")
                    .extracting(ex -> ((BibliotecaException) ex).codigo())
                    .isEqualTo("LIVRO_NAO_ENCONTRADO");
        }

        @Test
        @DisplayName("sem exemplares: LivroIndisponivelException")
        void indisponivel() {
            biblioteca.emprestar(JAVA_EFETIVO.isbn(), "ana", DIA_1);
            assertThatThrownBy(() -> biblioteca.emprestar(JAVA_EFETIVO.isbn(), "bruno", DIA_1))
                    .isInstanceOf(LivroIndisponivelException.class);
        }

        @Test
        @DisplayName("máximo de 3 empréstimos ativos por pessoa")
        void limite() {
            for (int i = 0; i < 3; i++) {
                biblioteca.emprestar(CODIGO_LIMPO.isbn(), "ana", DIA_1);
            }
            assertThatThrownBy(() -> biblioteca.emprestar(CODIGO_LIMPO.isbn(), "ana", DIA_1))
                    .isInstanceOf(LimiteEmprestimosException.class)
                    .hasMessageContaining("3");
            assertThat(biblioteca.disponiveis(CODIGO_LIMPO.isbn())).as("a falha não consome exemplar").isEqualTo(2);
        }

        @Test
        @DisplayName("quem tem empréstimo atrasado não pega outro")
        void atrasado() {
            biblioteca.emprestar(CODIGO_LIMPO.isbn(), "ana", DIA_1);
            LocalDate depoisDoPrazo = DIA_1.plusDays(15);
            assertThatThrownBy(() -> biblioteca.emprestar(JAVA_EFETIVO.isbn(), "ana", depoisDoPrazo))
                    .isInstanceOf(UsuarioComAtrasoException.class)
                    .extracting(ex -> ((BibliotecaException) ex).codigo())
                    .isEqualTo("USUARIO_COM_ATRASO");
        }
    }

    @Nested
    @DisplayName("devolução")
    class Devolver {
        @Test
        @DisplayName("no prazo: sem multa e o exemplar volta")
        void noPrazo() {
            Emprestimo e = biblioteca.emprestar(JAVA_EFETIVO.isbn(), "ana", DIA_1);
            assertThat(biblioteca.devolver(e.id(), DIA_1.plusDays(14))).isEqualTo(new BigDecimal("0.00"));
            assertThat(biblioteca.disponiveis(JAVA_EFETIVO.isbn())).isEqualTo(1);
        }

        @Test
        @DisplayName("atraso: R$ 2,00 por dia")
        void multa() {
            Emprestimo e = biblioteca.emprestar(JAVA_EFETIVO.isbn(), "ana", DIA_1);
            assertThat(biblioteca.devolver(e.id(), DIA_1.plusDays(14 + 3))).isEqualTo(new BigDecimal("6.00"));
        }

        @Test
        @DisplayName("multa tem teto de R$ 50,00")
        void teto() {
            Emprestimo e = biblioteca.emprestar(JAVA_EFETIVO.isbn(), "ana", DIA_1);
            assertThat(biblioteca.devolver(e.id(), DIA_1.plusDays(90))).isEqualTo(new BigDecimal("50.00"));
        }

        @Test
        @DisplayName("devolver duas vezes ou id inexistente: EmprestimoNaoEncontradoException")
        void duasVezes() {
            Emprestimo e = biblioteca.emprestar(JAVA_EFETIVO.isbn(), "ana", DIA_1);
            biblioteca.devolver(e.id(), DIA_1);
            assertThatThrownBy(() -> biblioteca.devolver(e.id(), DIA_1)).isInstanceOf(EmprestimoNaoEncontradoException.class);
            assertThatThrownBy(() -> biblioteca.devolver(999, DIA_1)).isInstanceOf(EmprestimoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("importação do acervo (exceção checked)")
    class Importacao {
        @Test
        @DisplayName("importa um CSV válido, ignorando linhas em branco")
        void valido() throws Exception {
            String csv = """
                    isbn;titulo;exemplares
                    111;Refatoração;2

                    222;Domain-Driven Design;1
                    """;
            Biblioteca nova = new Biblioteca();
            int importados = new ImportadorAcervo(nova).importar(new StringReader(csv));
            assertThat(importados).isEqualTo(2);
            assertThat(nova.disponiveis("111")).isEqualTo(2);
        }

        @Test
        @DisplayName("erro de formato informa a linha e não importa NADA (tudo ou nada)")
        void invalido() {
            String csv = """
                    isbn;titulo;exemplares
                    111;Refatoração;2
                    222;Sem exemplares
                    """;
            Biblioteca nova = new Biblioteca();
            assertThatThrownBy(() -> new ImportadorAcervo(nova).importar(new StringReader(csv)))
                    .isInstanceOf(ArquivoAcervoInvalidoException.class)
                    .hasMessageContaining("Linha 3")
                    .extracting(ex -> ((ArquivoAcervoInvalidoException) ex).linha())
                    .isEqualTo(3);
            assertThatThrownBy(() -> nova.disponiveis("111")).isInstanceOf(LivroNaoEncontradoException.class);
        }

        @Test
        @DisplayName("quantidade não numérica ou menor que 1 é erro")
        void quantidade() {
            String csv = "isbn;titulo;exemplares\n111;Livro;zero\n";
            assertThatThrownBy(() -> new ImportadorAcervo(new Biblioteca()).importar(new StringReader(csv)))
                    .isInstanceOf(ArquivoAcervoInvalidoException.class)
                    .hasMessageContaining("Linha 2")
                    .hasCauseInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("importa de um arquivo em disco")
        void arquivo(@TempDir Path pasta) throws Exception {
            Path arquivo = pasta.resolve("acervo.csv");
            Files.writeString(arquivo, "isbn;titulo;exemplares\n333;Arquitetura Limpa;4\n");
            Biblioteca nova = new Biblioteca();
            assertThat(new ImportadorAcervo(nova).importar(arquivo)).isEqualTo(1);
        }

        @Test
        @DisplayName("arquivo inexistente vira ArquivoAcervoInvalidoException com a causa original")
        void arquivoInexistente(@TempDir Path pasta) {
            assertThatThrownBy(() -> new ImportadorAcervo(new Biblioteca()).importar(pasta.resolve("nao-existe.csv")))
                    .isInstanceOf(ArquivoAcervoInvalidoException.class)
                    .hasCauseInstanceOf(java.io.IOException.class);
        }
    }
}
