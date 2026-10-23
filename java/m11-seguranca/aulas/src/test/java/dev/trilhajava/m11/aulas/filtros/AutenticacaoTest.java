package dev.trilhajava.m11.aulas.filtros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** As peças de autenticação montadas à mão, sem Spring Boot: o que o BasicAuthenticationFilter faz por dentro. */
class AutenticacaoTest {

    SegurancaConfig config = new SegurancaConfig();
    PasswordEncoder codificador = config.codificadorDeSenhas();

    AuthenticationManager gerenciador() {
        DaoAuthenticationProvider provedor = new DaoAuthenticationProvider(config.usuarios(codificador));
        provedor.setPasswordEncoder(codificador);
        return new ProviderManager(provedor);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void credencialCorretaViraUmAuthenticationComAutoridades() {
        Authentication pedido = UsernamePasswordAuthenticationToken.unauthenticated("recepcao", "recepcao-123");

        Authentication resultado = gerenciador().authenticate(pedido);

        System.out.println("autenticado=" + resultado.isAuthenticated()
                + " nome=" + resultado.getName()
                + " autoridades=" + resultado.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList()
                + " credenciais=" + resultado.getCredentials());

        assertThat(resultado.isAuthenticated()).isTrue();
        assertThat(resultado.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_RECEPCAO", "FACTOR_PASSWORD");
        assertThat(resultado.getCredentials()).as("a senha é apagada depois de conferida").isNull();
    }

    @Test
    void senhaErradaEUsuarioInexistenteSaoIndistinguiveis() {
        AuthenticationManager gerenciador = gerenciador();

        Throwable senhaErrada = catchThrowable(() -> gerenciador
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated("recepcao", "chute")));
        Throwable inexistente = catchThrowable(() -> gerenciador
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated("ninguem", "chute")));

        assertThat(senhaErrada).isInstanceOf(BadCredentialsException.class);
        assertThat(inexistente).isInstanceOf(BadCredentialsException.class)
                .hasMessage(senhaErrada.getMessage());
    }

    @Test
    void oResultadoVaiParaOSecurityContextDaThread() {
        Authentication resultado = gerenciador()
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated("dra.helena", "medica-123"));

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(resultado);
        SecurityContextHolder.setContext(contexto);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("dra.helena");
    }
}
