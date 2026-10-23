package dev.trilhajava.m11.senhas;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RepositorioDeUsuariosEmMemoria implements RepositorioDeUsuarios {

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();

    @Override
    public Optional<Usuario> buscar(String email) {
        return Optional.ofNullable(usuarios.get(email));
    }

    @Override
    public void salvar(Usuario usuario) {
        usuarios.put(usuario.email(), usuario);
    }
}
