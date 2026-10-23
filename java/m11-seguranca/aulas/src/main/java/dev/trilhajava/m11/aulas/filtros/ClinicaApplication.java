package dev.trilhajava.m11.aulas.filtros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aula 11.02: a API da clínica com duas cadeias de filtros.
 *
 * <ul>
 *   <li>{@code /api/**}: API stateless, HTTP Basic, sem CSRF, erros em Problem Details;</li>
 *   <li>todo o resto: console administrativo com login por formulário, sessão e CSRF.</li>
 * </ul>
 *
 * Para ver a lista de filtros de cada cadeia na subida, rode com
 * {@code --logging.level.org.springframework.security.web.DefaultSecurityFilterChain=DEBUG}.
 */
@SpringBootApplication
public class ClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicaApplication.class, args);
    }
}
