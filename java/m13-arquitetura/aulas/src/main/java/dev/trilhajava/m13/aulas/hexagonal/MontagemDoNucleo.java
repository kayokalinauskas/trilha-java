package dev.trilhajava.m13.aulas.hexagonal;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.ServicoDeResgate;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.entrada.ResgatarRecompensa;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CarteirasDePontos;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.CatalogoDeRecompensas;
import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.PublicadorDeEventos;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * O único lugar que conhece o núcleo E o Spring: cria o serviço com new, liga as portas aos
 * adaptadores e abre a transação em volta do caso de uso.
 */
@Configuration(proxyBeanMethods = false)
class MontagemDoNucleo {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    ResgatarRecompensa resgatarRecompensa(CarteirasDePontos carteiras, CatalogoDeRecompensas catalogo,
                                          PublicadorDeEventos eventos, Clock relogio,
                                          PlatformTransactionManager transacoes) {
        ServicoDeResgate servico = new ServicoDeResgate(carteiras, catalogo, eventos, relogio);
        TransactionTemplate transacao = new TransactionTemplate(transacoes);
        return comando -> transacao.execute(status -> servico.resgatar(comando));
    }
}
