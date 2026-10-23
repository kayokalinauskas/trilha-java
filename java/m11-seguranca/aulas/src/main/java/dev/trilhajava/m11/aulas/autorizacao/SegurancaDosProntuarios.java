package dev.trilhajava.m11.aulas.autorizacao;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Os três níveis de autorização:
 * ROTA (aqui), MÉTODO (@PreAuthorize nos serviços, ligado por @EnableMethodSecurity) e OBJETO (no serviço).
 */
@Configuration
@EnableMethodSecurity
public class SegurancaDosProntuarios {

    @Bean
    SecurityFilterChain prontuarios(HttpSecurity http, MesmaClinicaDaRota mesmaClinica) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers("/clinicas/{clinicaId}/**").access(AuthorizationManagers.allOf(
                                AuthorityAuthorizationManager.<RequestAuthorizationContext>hasRole("ATENDENTE"),
                                mesmaClinica))
                        .anyRequest().authenticated())                    // negar por padrão
                .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDeToken())))
                .build();
    }

    /** Claim "papeis": ["MEDICO"] vira a autoridade ROLE_MEDICO. */
    public static JwtGrantedAuthoritiesConverter papeis() {
        var papeis = new JwtGrantedAuthoritiesConverter();
        papeis.setAuthoritiesClaimName("papeis");
        papeis.setAuthorityPrefix("ROLE_");
        return papeis;
    }

    static JwtAuthenticationConverter conversorDeToken() {
        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(papeis());
        return conversor;
    }

    @Bean
    JwtDecoder decodificador(@Value("${prontuarios.jwt.segredo}") String segredo) {
        var chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(chave).build();
    }

    /** Sem este bean, o Spring Security não publica AuthorizationDeniedEvent (e a auditoria não vê as negações). */
    @Bean
    AuthorizationEventPublisher publicadorDeEventosDeAutorizacao(ApplicationEventPublisher publicador) {
        return new SpringAuthorizationEventPublisher(publicador);
    }
}
