package dev.trilhajava.m11.aulas.oidc.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * O Keycloak põe os papéis de cliente em resource_access.&lt;cliente&gt;.roles:
 *
 * <pre>
 * "resource_access": { "api-pedidos": { "roles": ["pedidos-leitura"] } }
 * </pre>
 *
 * Este conversor lê só os papéis do cliente desta API e os transforma em ROLE_*, para
 * usar hasRole(...). Papéis de outros clientes no mesmo token são ignorados de propósito.
 */
public class PapeisDoKeycloak implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String clienteDaApi;

    public PapeisDoKeycloak(String clienteDaApi) {
        this.clienteDaApi = clienteDaApi;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> acessos = jwt.getClaimAsMap("resource_access");
        if (acessos == null || !(acessos.get(clienteDaApi) instanceof Map<?, ?> desta)) {
            return List.of();
        }
        if (!(desta.get("roles") instanceof Collection<?> papeis)) {
            return List.of();
        }
        return papeis.stream()
                .map(papel -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + papel))
                .toList();
    }
}
