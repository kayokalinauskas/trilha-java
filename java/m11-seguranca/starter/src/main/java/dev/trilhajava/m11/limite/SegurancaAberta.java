package dev.trilhajava.m11.limite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Este exercício é sobre limite de requisições; a autenticação fica de fora (tudo liberado). */
@Configuration
public class SegurancaAberta {

    @Bean
    SecurityFilterChain aberta(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(rotas -> rotas.anyRequest().permitAll()).build();
    }
}
