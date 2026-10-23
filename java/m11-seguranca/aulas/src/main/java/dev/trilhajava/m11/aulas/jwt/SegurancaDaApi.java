package dev.trilhajava.m11.aulas.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(PropriedadesDoToken.class)
public class SegurancaDaApi {

    /** Stateless: sem sessão e sem cookie (por isso sem CSRF); a identidade vem só do Bearer token. */
    @Bean
    SecurityFilterChain api(HttpSecurity http, JwtAuthenticationConverter autoridadesDoToken) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.GET, "/contas/minha/extrato").hasAuthority("SCOPE_extrato:ler")
                        .requestMatchers(HttpMethod.POST, "/contas/minha/pix").hasAuthority("SCOPE_pix:enviar")
                        .requestMatchers("/backoffice/**").hasRole("SUPORTE")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(autoridadesDoToken)))
                .build();
    }

    /**
     * Valida, nesta ordem: o algoritmo (só RS256), a assinatura com a chave do JWKS indicada pelo "kid",
     * o "typ" do cabeçalho, exp/nbf com tolerância, o emissor ("iss") e a audiência ("aud").
     */
    @Bean
    JwtDecoder jwtDecoder(PropriedadesDoToken propriedades) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(propriedades.jwkSetUri())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                new JwtTimestampValidator(propriedades.toleranciaDoRelogio()),
                new JwtIssuerValidator(propriedades.emissor()),
                new JwtAudienceValidator(propriedades.audiencia())));
        return decoder;
    }

    /**
     * "scope": "extrato:ler pix:enviar" vira SCOPE_extrato:ler e SCOPE_pix:enviar (o padrão do Spring);
     * "roles": ["SUPORTE"] vira ROLE_SUPORTE, o que hasRole("SUPORTE") espera.
     */
    @Bean
    JwtAuthenticationConverter autoridadesDoToken() {
        var escopos = new JwtGrantedAuthoritiesConverter();      // lê "scope" ou "scp", prefixo SCOPE_
        var papeis = new JwtGrantedAuthoritiesConverter();
        papeis.setAuthoritiesClaimName("roles");
        papeis.setAuthorityPrefix("ROLE_");

        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(new DelegatingJwtGrantedAuthoritiesConverter(escopos, papeis));
        conversor.setPrincipalClaimName("sub");                  // getName() devolve o id do cliente
        return conversor;
    }
}
