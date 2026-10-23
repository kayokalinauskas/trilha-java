package dev.trilhajava.m11.aulas.senhas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class CalibracaoDeCustoTest {

    @Test
    @DisplayName("Cada ponto de custo do BCrypt dobra o trabalho: custo 12 leva bem mais que custo 10")
    void custoDobra() {
        double custo10 = CalibracaoDeCusto.medianaMs(new BCryptPasswordEncoder(10), 3);
        double custo12 = CalibracaoDeCusto.medianaMs(new BCryptPasswordEncoder(12), 3);

        // Em teoria, 4 vezes. A margem larga evita um teste instável em máquinas ocupadas.
        assertThat(custo12).isGreaterThan(custo10 * 2);
    }
}
