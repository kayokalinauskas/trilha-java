package dev.trilhajava.fintech.api;

import dev.trilhajava.fintech.aplicacao.Solicitante;
import java.util.UUID;
import org.springframework.security.core.Authentication;

/** Traduz o usuário autenticado (o token já validado) para o Solicitante dos casos de uso. */
final class Solicitantes {

    private Solicitantes() {
    }

    static Solicitante de(Authentication autenticacao) {
        UUID usuario = UUID.fromString(autenticacao.getName());     // com JWT, getName() é o "sub"
        boolean admin = autenticacao.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return new Solicitante(usuario, admin);
    }
}
