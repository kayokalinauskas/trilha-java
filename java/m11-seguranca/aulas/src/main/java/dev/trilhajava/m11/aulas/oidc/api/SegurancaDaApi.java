package dev.trilhajava.m11.aulas.oidc.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource Server: valida o access token (assinatura pelo JWKS, exp, iss e aud, estes dois
 * configurados no api-de-pedidos.yml) e decide pelo escopo. Sem sessão e sem login: cada
 * requisição traz o próprio token.
 */
@Configuration
class SegurancaDaApi {

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(regras -> regras
                        // o claim "scope" vira autoridades SCOPE_*: pedidos:ler -> SCOPE_pedidos:ler
                        .requestMatchers(HttpMethod.GET, "/pedidos/**").hasAuthority("SCOPE_pedidos:ler")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable()); // sem cookie de sessão, não há o que forjar
        return http.build();
    }
}
