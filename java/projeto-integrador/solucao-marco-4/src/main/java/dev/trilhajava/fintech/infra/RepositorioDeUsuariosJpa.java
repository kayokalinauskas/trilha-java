package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.dominio.RepositorioDeUsuarios;
import dev.trilhajava.fintech.dominio.Usuario;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class RepositorioDeUsuariosJpa implements RepositorioDeUsuarios {

    private final UsuarioJpa jpa;

    RepositorioDeUsuariosJpa(UsuarioJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Usuario usuario) {
        jpa.saveAndFlush(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpa.existsByEmail(email);
    }
}
