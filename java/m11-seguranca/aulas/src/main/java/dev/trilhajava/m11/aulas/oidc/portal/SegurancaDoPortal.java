package dev.trilhajava.m11.aulas.oidc.portal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cliente web (oauth2Login): o navegador só recebe um cookie de sessão; os tokens ficam no
 * servidor. O CSRF continua ligado, porque a sessão é um cookie.
 */
@Configuration
class SegurancaDoPortal {

    @Bean
    SecurityFilterChain portal(HttpSecurity http, ClientRegistrationRepository registros) throws Exception {
        var logoutNoKeycloak = new OidcClientInitiatedLogoutSuccessHandler(registros);
        logoutNoKeycloak.setPostLogoutRedirectUri("{baseUrl}/");

        http
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(logoutNoKeycloak));
        return http.build();
    }
}
