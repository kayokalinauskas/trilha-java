package dev.trilhajava.fintech.infra;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação stateless com JWT (OAuth2 Resource Server). A AUTORIZAÇÃO por dono não está aqui:
 * ela depende do objeto (de quem é a conta?) e mora nos casos de uso.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SegurancaConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http, RespostasDeSeguranca respostas,
            Converter<Jwt, AbstractAuthenticationToken> papeisDoToken) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())   // sem cookies de sessão, não há o que um site malicioso "pegue carona"
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/usuarios", "/auth/token").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/docs", "/docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/error").permitAll()
                        // Marco 6: health e probes públicos (o Kubernetes não manda token); os detalhes
                        // só aparecem para ADMIN (management.endpoint.health.show-details=when-authorized).
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").denyAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(papeisDoToken))
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas))
                .exceptionHandling(erros -> erros.authenticationEntryPoint(respostas).accessDeniedHandler(respostas))
                .build();
    }

    /** {bcrypt} por padrão, e o prefixo permite migrar de algoritmo sem invalidar as senhas antigas. */
    @Bean
    PasswordEncoder codificadorDeSenhas() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecretKey chaveDeAssinatura(JwtProperties propriedades) {
        return new SecretKeySpec(propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** Assinatura HS256 (só HS256: "alg: none" e outros algoritmos são recusados), expiração e emissor. */
    @Bean
    JwtDecoder jwtDecoder(SecretKey chave, JwtProperties propriedades) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(propriedades.emissor()));
        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chave) {
        return NimbusJwtEncoder.withSecretKey(chave).build();
    }

    /** O claim "roles" (["ADMIN"]) vira a autoridade ROLE_ADMIN. */
    @Bean
    Converter<Jwt, AbstractAuthenticationToken> papeisDoToken() {
        var papeis = new JwtGrantedAuthoritiesConverter();
        papeis.setAuthoritiesClaimName("roles");
        papeis.setAuthorityPrefix("ROLE_");
        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(papeis);
        return conversor;
    }
}
