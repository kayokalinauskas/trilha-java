package dev.trilhajava.m11.rh;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação: HTTP Basic, sem sessão. Tudo exige login.
 * A AUTORIZAÇÃO (quem pode ver ou mudar qual funcionário) depende do objeto — por isso mora no serviço.
 */
@Configuration
public class SegurancaDoRh {

    @Bean
    SecurityFilterChain rh(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
