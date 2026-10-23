package dev.trilhajava.m11.senhas;

import java.util.Optional;

public interface RepositorioDeUsuarios {

    Optional<Usuario> buscar(String email);

    void salvar(Usuario usuario);
}
