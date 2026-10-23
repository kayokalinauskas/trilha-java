package dev.trilhajava.m12.aulas.rabbitmq;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica ExameConcluido na exchange "clinica.exames" com a routing key "tipo.unidade".
 * Com publisher confirms ligados (spring.rabbitmq.publisher-confirm-type=correlated) e
 * mandatory=true, publicarComConfirmacao só retorna depois que o broker aceitou a mensagem.
 */
@Component
public class PublicadorDeExames {

    private final RabbitTemplate rabbit;

    public PublicadorDeExames(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    /** "Dispare e esqueça": escreve no socket e retorna, sem esperar o broker. */
    public void publicar(ExameConcluido exame) {
        rabbit.convertAndSend(TopologiaDeExames.EXCHANGE, exame.chaveDeRoteamento(), exame, comMessageId(exame));
    }

    /**
     * Publica e espera a confirmação do broker. Lança PublicacaoNaoConfirmadaException se o
     * broker recusar (nack), se a mensagem não tiver rota (return) ou se a confirmação não chegar
     * dentro do prazo. Quem chama decide: tentar de novo, devolver erro ou gravar para depois.
     */
    public void publicarComConfirmacao(ExameConcluido exame, Duration prazo) {
        var correlacao = new CorrelationData(exame.id().toString());
        rabbit.convertAndSend(TopologiaDeExames.EXCHANGE, exame.chaveDeRoteamento(), exame,
                comMessageId(exame), correlacao);
        try {
            CorrelationData.Confirm confirmacao = correlacao.getFuture().get(prazo.toMillis(), TimeUnit.MILLISECONDS);
            if (correlacao.getReturned() != null) {
                // Com mandatory=true, o broker devolve (basic.return) antes de confirmar: ack sem fila.
                throw new PublicacaoNaoConfirmadaException(exame.id(),
                        "sem fila de destino: " + correlacao.getReturned().getReplyText());
            }
            if (!confirmacao.ack()) {
                throw new PublicacaoNaoConfirmadaException(exame.id(), "nack do broker: " + confirmacao.reason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PublicacaoNaoConfirmadaException(exame.id(), "interrompido esperando a confirmação");
        } catch (ExecutionException | TimeoutException e) {
            throw new PublicacaoNaoConfirmadaException(exame.id(), "sem confirmação em " + prazo);
        }
    }

    /** O id viaja no header padrão message_id: o consumidor deduplica sem abrir o corpo. */
    private static MessagePostProcessor comMessageId(ExameConcluido exame) {
        return mensagem -> {
            mensagem.getMessageProperties().setMessageId(exame.id().toString());
            return mensagem;
        };
    }
}
