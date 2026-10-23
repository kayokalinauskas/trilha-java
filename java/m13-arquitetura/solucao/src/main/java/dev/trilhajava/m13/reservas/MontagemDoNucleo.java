package dev.trilhajava.m13.reservas;

import dev.trilhajava.m13.reservas.aplicacao.ServicoDeReservas;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.CatalogoDeSalas;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.Notificador;
import dev.trilhajava.m13.reservas.aplicacao.porta.saida.RepositorioDeReservas;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O único lugar que conhece o núcleo E o Spring: registra o serviço como bean, ligando as portas
 * de saída aos adaptadores que o Spring encontrou. O núcleo continua sem nenhuma anotação.
 */
@Configuration
class MontagemDoNucleo {

    @Bean
    ServicoDeReservas servicoDeReservas(RepositorioDeReservas reservas, CatalogoDeSalas salas,
            Notificador notificador, Clock relogio) {
        return new ServicoDeReservas(reservas, salas, notificador, relogio);
    }
}
