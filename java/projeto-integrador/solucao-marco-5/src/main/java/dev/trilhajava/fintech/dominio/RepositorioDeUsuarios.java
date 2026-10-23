package dev.trilhajava.fintech.dominio;

import java.util.Optional;

public interface RepositorioDeUsuarios {

    void salvar(Usuario usuario);

    /** {@code email} já normalizado ({@link Usuario#normalizarEmail}). */
    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);
}
