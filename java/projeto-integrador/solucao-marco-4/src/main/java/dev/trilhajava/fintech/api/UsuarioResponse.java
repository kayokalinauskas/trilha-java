package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.dominio.Papel;
import dev.trilhajava.fintech.dominio.Usuario;
import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, Papel papel) {

    static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.papel());
    }
}
