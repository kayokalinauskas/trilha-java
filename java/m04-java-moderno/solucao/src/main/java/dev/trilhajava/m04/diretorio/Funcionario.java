package dev.trilhajava.m04.diretorio;

import java.util.Optional;

/**
 * Funcionário vindo do sistema de RH. E-mail e gestor podem faltar (null no cadastro legado).
 * Repare: os campos continuam nulos internamente, mas a API pública expõe Optional.
 */
public record Funcionario(String matricula, String nome, String email, String matriculaGestor, boolean ativo) {

    public Optional<String> emailOpcional() {
        return Optional.ofNullable(email).filter(e -> !e.isBlank());
    }

    public Optional<String> gestor() {
        return Optional.ofNullable(matriculaGestor);
    }
}
