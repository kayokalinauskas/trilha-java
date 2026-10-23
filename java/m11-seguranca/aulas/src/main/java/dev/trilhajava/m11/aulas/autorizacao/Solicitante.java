package dev.trilhajava.m11.aulas.autorizacao;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Quem está pedindo, já traduzido do token: usuário (claim sub), clínica (claim "clinica") e papel.
 * O resto do código recebe este record explícito, e não o SecurityContextHolder.
 */
public record Solicitante(String usuario, String clinicaId, Papel papel) {

    public static Solicitante de(Authentication autenticacao) {
        if (!(autenticacao instanceof JwtAuthenticationToken token)) {
            throw new IllegalStateException("Esperava um JWT, veio " + autenticacao);
        }
        String clinica = token.getToken().getClaimAsString("clinica");
        if (clinica == null) {
            throw new IllegalStateException("Token sem a claim clinica");
        }
        Papel papel = token.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> Papel.valueOf(a.substring("ROLE_".length())))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Token sem papel"));
        return new Solicitante(token.getName(), clinica, papel);
    }

    public boolean eh(Papel outro) {
        return papel == outro;
    }
}
