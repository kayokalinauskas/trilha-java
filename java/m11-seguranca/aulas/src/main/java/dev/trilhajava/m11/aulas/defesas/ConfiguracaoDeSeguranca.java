package dev.trilhajava.m11.aulas.defesas;

import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ConfiguracaoDeSeguranca {

    @Bean
    SecurityFilterChain api(HttpSecurity http, LimitadorDeCreditos limitador, JsonMapper json) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())                    // API stateless, Bearer token, sem cookie
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())                 // usa o bean UrlBasedCorsConfigurationSource
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.GET, "/api/cotacoes").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor.jwt(Customizer.withDefaults()))
                .headers(headers -> headers
                        // Uma API JSON não carrega script, estilo nem imagem: nada é permitido.
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER)))
                // Depois da autenticação (o filtro precisa saber QUEM chama) e antes da autorização.
                .addFilterAfter(new FiltroDeCota(limitador, json), AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://app.contadigital.dev"));    // origens exatas, nunca "*"
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
        // Sem isto, o JavaScript do front NÃO consegue ler estes headers, mesmo que a resposta os traga.
        config.setExposedHeaders(List.of("Retry-After", "RateLimit", "RateLimit-Policy", "Location"));
        config.setAllowCredentials(false);                    // o token vai no header Authorization, não em cookie
        config.setMaxAge(Duration.ofHours(1));                // o navegador guarda o preflight por 1 hora
        var fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", config);
        return fonte;
    }
}
