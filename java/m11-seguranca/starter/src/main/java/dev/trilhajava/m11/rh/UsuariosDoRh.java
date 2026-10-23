package dev.trilhajava.m11.rh;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/** Logins de exemplo: a senha é "senha-de-" + usuário. Papéis: RH, GESTOR ou FUNCIONARIO. */
@Configuration
public class UsuariosDoRh {

    @Bean
    PasswordEncoder codificadorDeSenhas() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService usuarios(PasswordEncoder codificador) {
        return new InMemoryUserDetailsManager(
                usuario(codificador, "rita", "RH"),
                usuario(codificador, "gustavo", "GESTOR"),
                usuario(codificador, "fernanda", "FUNCIONARIO"),
                usuario(codificador, "felipe", "FUNCIONARIO"),
                usuario(codificador, "gabriela", "GESTOR"),
                usuario(codificador, "fabio", "FUNCIONARIO"));
    }

    private static org.springframework.security.core.userdetails.UserDetails usuario(
            PasswordEncoder codificador, String nome, String papel) {
        return User.withUsername(nome).password(codificador.encode("senha-de-" + nome)).roles(papel).build();
    }
}
