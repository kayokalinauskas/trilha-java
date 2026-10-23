package dev.trilhajava.m13.aulas.solid;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/** Liga as peças: varre este pacote (regras, caso de uso, adaptadores) e fornece o relógio. */
@Configuration
@ComponentScan
public class ConfiguracaoDaLoja {

    @Bean
    Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
