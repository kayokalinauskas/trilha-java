package dev.trilhajava.m11.aulas.oidc.portal;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MinhaContaController {

    @GetMapping("/")
    String inicio() {
        return "Portal da loja";
    }

    /** OidcUser: a identidade que veio no ID token (e no userinfo, que o Spring consulta). */
    @GetMapping("/minha-conta")
    Map<String, Object> minhaConta(@AuthenticationPrincipal OidcUser usuario) {
        return Map.of(
                "id", usuario.getSubject(),
                "nome", usuario.getFullName(),
                "email", usuario.getEmail());
    }
}
