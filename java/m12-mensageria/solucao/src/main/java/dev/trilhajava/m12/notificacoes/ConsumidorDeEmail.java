package dev.trilhajava.m12.notificacoes;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consome a fila de e-mail. Falhas transitórias sobem como exceção e o retry do container
 * repete; esgotadas as tentativas (ou numa falha permanente), a mensagem é rejeitada sem
 * requeue e o RabbitMQ a encaminha para a DLX.
 */
@Component
class ConsumidorDeEmail {

    private final EnviadorDeEmail enviador;

    ConsumidorDeEmail(EnviadorDeEmail enviador) {
        this.enviador = enviador;
    }

    @RabbitListener(queues = TopologiaRabbit.FILA_EMAIL)
    void receber(Notificacao notificacao) {
        if (!notificacao.destinatario().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new DestinatarioInvalidoException(notificacao.destinatario());
        }
        enviador.enviar(notificacao.destinatario(), notificacao.mensagem());
    }
}
