package dev.trilhajava.m12.aulas.rabbitmq;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consome os avisos com ack MANUAL: o código decide, caso a caso, entre ack, nack com
 * requeue e nack sem requeue. Útil para ver o protocolo; no dia a dia, o modo AUTO basta.
 */
@Component
public class ConsumidorDeAvisos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorDeAvisos.class);

    private final NotificadorDePaciente notificador;

    public ConsumidorDeAvisos(NotificadorDePaciente notificador) {
        this.notificador = notificador;
    }

    @RabbitListener(id = "avisos", queues = TopologiaDeExames.FILA_AVISOS, ackMode = "MANUAL")
    public void receber(ExameConcluido exame, Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long etiqueta) throws IOException {
        try {
            notificador.avisar(exame);
            canal.basicAck(etiqueta, false);                  // pronto: o broker apaga a mensagem
        } catch (PacienteSemContatoException e) {
            log.warn("Aviso descartado para a DLQ: {}", e.getMessage());
            canal.basicNack(etiqueta, false, false);          // sem requeue: vai para a DLX
        } catch (NotificadorIndisponivelException e) {
            log.warn("Provedor fora; a mensagem volta para a fila: {}", e.getMessage());
            canal.basicNack(etiqueta, false, true);           // requeue: a quorum queue conta (delivery-limit)
        }
    }
}
