package dev.trilhajava.m13.aulas.hexagonal.adaptador.mensageria;

import dev.trilhajava.m13.aulas.hexagonal.aplicacao.porta.saida.PublicadorDeEventos;
import dev.trilhajava.m13.aulas.hexagonal.dominio.RecompensaResgatada;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saída para eventos. Esta versão entrega o evento dentro da própria aplicação;
 * trocar por Kafka (com transactional outbox) é escrever outra classe que implementa a mesma porta.
 */
@Component
class PublicadorDeEventosSpring implements PublicadorDeEventos {

    private final ApplicationEventPublisher publicador;

    PublicadorDeEventosSpring(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @Override
    public void publicar(RecompensaResgatada evento) {
        publicador.publishEvent(evento);
    }
}
