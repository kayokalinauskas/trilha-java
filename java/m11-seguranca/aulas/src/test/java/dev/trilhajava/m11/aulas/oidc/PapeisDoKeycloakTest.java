package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m11.aulas.oidc.api.PapeisDoKeycloak;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@DisplayName("Papéis de cliente do Keycloak viram ROLE_*")
class PapeisDoKeycloakTest {

    private final PapeisDoKeycloak conversor = new PapeisDoKeycloak("api-pedidos");

    private static Jwt tokenCom(Map<String, Object> claims) {
        var token = Jwt.withTokenValue("token-de-teste")
                .header("alg", "RS256")
                .issuedAt(Instant.parse("2026-10-05T12:00:00Z"))
                .expiresAt(Instant.parse("2026-10-05T12:05:00Z"));
        claims.forEach(token::claim);
        return token.build();
    }

    @Test
    @DisplayName("lê só os papéis do cliente desta API, ignorando os de outros clientes")
    void papeisDestaApi() {
        Jwt jwt = tokenCom(Map.of(
                "sub", "service-account-servico-de-faturamento",
                "resource_access", Map.of(
                        "api-pedidos", Map.of("roles", List.of("pedidos-leitura")),
                        "api-estoque", Map.of("roles", List.of("estoque-admin")))));

        assertThat(conversor.convert(jwt))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_pedidos-leitura");
    }

    @Test
    @DisplayName("sem resource_access (ou sem papéis desta API), nenhuma autoridade")
    void semPapeis() {
        assertThat(conversor.convert(tokenCom(Map.of("sub", "ana")))).isEmpty();
        assertThat(conversor.convert(tokenCom(Map.of("sub", "ana",
                "resource_access", Map.of("api-estoque", Map.of("roles", List.of("estoque-admin")))))))
                .isEmpty();
    }
}
