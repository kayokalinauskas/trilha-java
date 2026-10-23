package dev.trilhajava.m04.diretorio;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Solução de referência do exercício 04.02. */
public class Diretorio {

    public static final String EMAIL_RH = "rh@empresa.com";

    private final Map<String, Funcionario> porMatricula;

    public Diretorio(List<Funcionario> funcionarios) {
        this.porMatricula = funcionarios.stream()
                .collect(Collectors.toUnmodifiableMap(Funcionario::matricula, Function.identity()));
    }

    public Optional<Funcionario> buscar(String matricula) {
        // Optional.ofNullable(matricula) evita o NPE do Map.get(null) em mapas imutáveis
        return Optional.ofNullable(matricula).map(porMatricula::get);
    }

    public Funcionario buscarObrigatorio(String matricula) {
        return buscar(matricula).orElseThrow(() -> new FuncionarioNaoEncontradoException(matricula));
    }

    public Optional<String> emailDoGestor(String matricula) {
        return buscar(matricula)
                .flatMap(Funcionario::gestor)        // Optional<String> matrícula do gestor
                .flatMap(this::buscar)               // Optional<Funcionario> gestor
                .flatMap(Funcionario::emailOpcional);
    }

    public String nomeParaExibicao(String matricula) {
        return buscar(matricula).map(Funcionario::nome).orElse("Funcionário não encontrado");
    }

    public String contatoPara(String matricula) {
        return buscar(matricula)
                .flatMap(Funcionario::emailOpcional)
                .or(() -> emailDoGestor(matricula))  // Optional.or (Java 9): alternativa preguiçosa
                .orElse(EMAIL_RH);
    }

    public Optional<Funcionario> superior(String matricula, int niveis) {
        Optional<Funcionario> atual = buscar(matricula);
        for (int i = 0; i < niveis; i++) {
            atual = atual.flatMap(Funcionario::gestor).flatMap(this::buscar);
        }
        return atual;
    }

    public List<String> emailsAtivos(List<String> matriculas) {
        return matriculas.stream()
                .map(this::buscar)
                .flatMap(Optional::stream)           // descarta os vazios
                .filter(Funcionario::ativo)
                .map(Funcionario::emailOpcional)
                .flatMap(Optional::stream)
                .toList();
    }
}
