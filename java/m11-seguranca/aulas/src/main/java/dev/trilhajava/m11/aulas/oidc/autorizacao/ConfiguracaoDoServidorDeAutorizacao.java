package dev.trilhajava.m11.aulas.oidc.autorizacao;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * O que o Spring Boot não adivinha: quem são as pessoas e o que vai dentro dos tokens.
 * Clientes, endpoints, chaves de assinatura e telas de login vêm da autoconfiguração.
 */
@Configuration
class ConfiguracaoDoServidorDeAutorizacao {

    /** A audiência da API de pedidos: o nome que ela confere no claim aud. */
    static final String API_DE_PEDIDOS = "api-pedidos";

    record Perfil(String nome, String email) {
    }

    /** Em produção, isto é um banco de usuários (ou uma federação com o AD da empresa). */
    private static final Map<String, Perfil> PERFIS = Map.of(
            "ana", new Perfil("Ana Souza", "ana@loja.test"));

    @Bean
    UserDetailsService usuarios(@Value("${loja.senha-da-ana}") String senhaDaAna) {
        return new InMemoryUserDetailsManager(
                User.withUsername("ana").password(senhaDaAna).roles("CLIENTE").build());
    }

    /**
     * Personaliza os JWTs na hora de assinar:
     * <ul>
     *   <li>access token com algum escopo "pedidos:*" ganha aud = api-pedidos (por padrão, o
     *       aud seria o client_id de quem pediu, que não serve para a API conferir);</li>
     *   <li>ID token ganha nome e e-mail, como pedem os escopos profile e email do OIDC.</li>
     * </ul>
     */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> conteudoDosTokens() {
        return contexto -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(contexto.getTokenType())
                    && contexto.getAuthorizedScopes().stream().anyMatch(escopo -> escopo.startsWith("pedidos:"))) {
                contexto.getClaims().audience(List.of(API_DE_PEDIDOS));
            }
            if (OidcParameterNames.ID_TOKEN.equals(contexto.getTokenType().getValue())) {
                Perfil perfil = PERFIS.get(contexto.getPrincipal().getName());
                if (perfil != null) {
                    contexto.getClaims().claim("name", perfil.nome()).claim("email", perfil.email());
                }
            }
        };
    }
}
