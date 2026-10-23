package dev.trilhajava.m12.notificacoes;

import java.util.Locale;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publica notificações na exchange "notificacoes" com a routing key "canal.prioridade". */
@Component
public class PublicadorDeNotificacoes {

    private final RabbitTemplate rabbit;

    public PublicadorDeNotificacoes(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    public void publicar(Notificacao notificacao) {
        rabbit.convertAndSend(TopologiaRabbit.EXCHANGE, chaveDeRoteamento(notificacao), notificacao, mensagem -> {
            // O id viaja no cabeçalho padrão message_id: o consumidor pode deduplicar sem abrir o corpo.
            mensagem.getMessageProperties().setMessageId(notificacao.id().toString());
            return mensagem;
        });
    }

    static String chaveDeRoteamento(Notificacao notificacao) {
        return (notificacao.canal() + "." + notificacao.prioridade()).toLowerCase(Locale.ROOT);
    }
}
