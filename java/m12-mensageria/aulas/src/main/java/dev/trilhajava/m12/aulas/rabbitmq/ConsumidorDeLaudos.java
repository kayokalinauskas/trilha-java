package dev.trilhajava.m12.aulas.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consome a fila de laudos com ack automático (AcknowledgeMode.AUTO): o Spring confirma
 * quando o método retorna e rejeita quando ele lança exceção.
 */
@Component
public class ConsumidorDeLaudos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorDeLaudos.class);

    private final GeradorDeLaudo gerador;

    public ConsumidorDeLaudos(GeradorDeLaudo gerador) {
        this.gerador = gerador;
    }

    // De 2 a 4 consumidores nesta fila, cada um com prefetch 1 (veja fabricaDeLaudos).
    @RabbitListener(id = "laudos", queues = TopologiaDeExames.FILA_LAUDOS,
            containerFactory = "fabricaDeLaudos", concurrency = "2-4")
    public void receber(ExameConcluido exame) {
        try {
            gerador.gerar(exame);
            log.info("Laudo gerado: exame={} paciente={}", exame.id(), exame.pacienteId());
        } catch (LaudoInvalidoException e) {
            // Permanente: rejeita SEM requeue. O RabbitMQ manda a mensagem para a DLX.
            throw new AmqpRejectAndDontRequeueException("Laudo impossível para o exame " + exame.id(), e);
        }
        // Qualquer outra exceção sobe como está: é tratada como transitória (retry do container).
    }
}
