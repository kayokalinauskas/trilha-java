package dev.trilhajava.m11.delivery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/** Usuários de exemplo (num sistema real, viriam do banco). */
@Configuration
public class UsuariosDeExemplo {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService usuarios(PasswordEncoder codificador) {
        return new InMemoryUserDetailsManager(
                User.withUsername("ana").password(codificador.encode("cafe-com-leite-123")).roles("CLIENTE").build(),
                User.withUsername("bruno").password(codificador.encode("moto-rapida-456")).roles("ENTREGADOR").build(),
                User.withUsername("carla").password(codificador.encode("painel-admin-789")).roles("ADMIN").build());
    }
}
