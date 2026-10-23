package dev.trilhajava.m11.aulas.filtros;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    /** Cadeia 1: a API. Só entra aqui o que casar com /api/**; o resto segue para a próxima cadeia. */
    @Bean
    @Order(1)
    SecurityFilterChain api(HttpSecurity http, RespostasDeSeguranca respostas) throws Exception {
        return http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers(GET, "/api/status").permitAll()
                        .requestMatchers(POST, "/api/consultas").hasRole("RECEPCAO")
                        .requestMatchers(GET, "/api/consultas/**").hasAnyRole("RECEPCAO", "MEDICO")
                        .anyRequest().denyAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(respostas))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas))
                .addFilterBefore(new FiltroDeAuditoria(), ExceptionTranslationFilter.class)
                .build();
    }

    /** Cadeia 2: o console web. Sem securityMatcher, casa com tudo; por isso vem por último. */
    @Bean
    @Order(2)
    SecurityFilterChain console(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .formLogin(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder codificadorDeSenhas() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Usuários de exemplo, em memória. Em produção, um UserDetailsService que lê do banco. */
    @Bean
    UserDetailsService usuarios(PasswordEncoder codificador) {
        return new InMemoryUserDetailsManager(
                User.withUsername("recepcao").password(codificador.encode("recepcao-123")).roles("RECEPCAO").build(),
                User.withUsername("dra.helena").password(codificador.encode("medica-123")).roles("MEDICO").build(),
                User.withUsername("admin").password(codificador.encode("admin-123")).roles("ADMIN").build());
    }
}
