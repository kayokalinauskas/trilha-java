package dev.trilhajava.m11.aulas.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

import dev.trilhajava.m11.aulas.oidc.portal.PortalApplication;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(classes = PortalApplication.class, properties = "spring.config.name=portal")
@AutoConfigureMockMvc
@DisplayName("Portal web com oauth2Login (OpenID Connect)")
class PortalTest {

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registro) {
        registro.add("EMISSOR", ServidorDeAutorizacaoDeTeste::emissor);
        registro.add("PORTAL_CLIENT_SECRET", () -> ServidorDeAutorizacaoDeTeste.SEGREDO_PORTAL);
    }

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ClientRegistrationRepository registros;

    @Test
    @DisplayName("sem sessão, a página protegida manda o navegador iniciar o login")
    void paginaProtegidaRedireciona() {
        assertThat(mvc.get().uri("/minha-conta"))
                .hasStatus3xxRedirection()
                .hasRedirectedUrl("/oauth2/authorization/portal");
    }

    @Test
    @DisplayName("o pedido de autorização leva state, nonce, PKCE S256 e a redirect_uri exata")
    void pedidoDeAutorizacao() {
        MvcTestResult resultado = mvc.get().uri("/oauth2/authorization/portal").exchange();

        assertThat(resultado).hasStatus3xxRedirection();
        String destino = resultado.getResponse().getRedirectedUrl();
        assertThat(destino).startsWith(ServidorDeAutorizacaoDeTeste.emissor() + "/oauth2/authorize?");

        Map<String, String> parametros = UriComponentsBuilder.fromUriString(destino).build(true)
                .getQueryParams().toSingleValueMap();
        assertThat(parametros)
                .containsEntry("response_type", "code")
                .containsEntry("client_id", "portal-web")
                .containsEntry("scope", "openid%20profile%20email%20pedidos:ler")
                .containsEntry("redirect_uri", "http://localhost/login/oauth2/code/portal")
                .containsEntry("code_challenge_method", "S256")
                .containsKeys("state", "nonce", "code_challenge");
    }

    @Test
    @DisplayName("com um usuário OIDC na sessão, a página mostra os dados do ID token")
    void minhaConta() {
        assertThat(mvc.get().uri("/minha-conta").with(oidcLogin()
                        .idToken(token -> token
                                .subject("8f2c1a7e-ana")
                                .claim("name", "Ana Souza")
                                .claim("email", "ana@loja.test"))))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.nome").isEqualTo("Ana Souza");
    }

    @Test
    @DisplayName("logout encerra a sessão local e manda o navegador ao end_session_endpoint do provedor")
    void logoutNoKeycloak() {
        MvcTestResult resultado = mvc.post().uri("/logout")
                .with(csrf())
                .with(oidcLogin()
                        .clientRegistration(registros.findByRegistrationId("portal"))
                        .idToken(token -> token.tokenValue("id-token-da-ana")))
                .exchange();

        assertThat(resultado).hasStatus3xxRedirection();
        UriComponents destino = UriComponentsBuilder.fromUriString(resultado.getResponse().getRedirectedUrl()).build();
        assertThat(destino.toUriString()).startsWith(ServidorDeAutorizacaoDeTeste.emissor() + "/connect/logout?");
        assertThat(destino.getQueryParams().toSingleValueMap())
                .containsEntry("id_token_hint", "id-token-da-ana")
                .containsEntry("post_logout_redirect_uri", "http://localhost/");
    }
}
