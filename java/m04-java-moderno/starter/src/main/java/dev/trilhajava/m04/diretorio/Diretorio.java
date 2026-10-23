package dev.trilhajava.m04.diretorio;

import java.util.List;
import java.util.Optional;

/**
 * Exercício 04.02 — Diretório de funcionários.
 * Rode: mvn test -Dtest=DiretorioTest
 *
 * Regra do exercício: nenhum "if (x == null)" e nenhum Optional.get() / isPresent().
 * Use map, flatMap, filter, or, orElse, orElseGet, orElseThrow e Optional::stream.
 */
public class Diretorio {

    public static final String EMAIL_RH = "rh@empresa.com";

    public Diretorio(List<Funcionario> funcionarios) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<Funcionario> buscar(String matricula) {
        throw new UnsupportedOperationException("TODO");
    }

    public Funcionario buscarObrigatorio(String matricula) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<String> emailDoGestor(String matricula) {
        throw new UnsupportedOperationException("TODO");
    }

    public String nomeParaExibicao(String matricula) {
        throw new UnsupportedOperationException("TODO");
    }

    /** E-mail do próprio funcionário; senão do gestor direto; senão EMAIL_RH. */
    public String contatoPara(String matricula) {
        throw new UnsupportedOperationException("TODO");
    }

    /** O superior N níveis acima (0 = o próprio). Vazio se a cadeia acabar antes. */
    public Optional<Funcionario> superior(String matricula, int niveis) {
        throw new UnsupportedOperationException("TODO");
    }

    /** E-mails dos funcionários existentes, ativos e com e-mail, na ordem das matrículas recebidas. */
    public List<String> emailsAtivos(List<String> matriculas) {
        throw new UnsupportedOperationException("TODO");
    }
}
