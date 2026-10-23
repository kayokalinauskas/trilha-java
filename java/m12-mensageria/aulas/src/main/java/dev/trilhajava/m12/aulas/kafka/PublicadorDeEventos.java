package dev.trilhajava.m12.aulas.kafka;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PublicadorDeEventos {

    private static final Duration PRAZO_DE_CONFIRMACAO = Duration.ofSeconds(10);

    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;

    public PublicadorDeEventos(KafkaTemplate<String, String> kafka, JsonMapper json) {
        this.kafka = kafka;
        this.json = json;
    }

    /**
     * Assíncrono: devolve na hora, e o futuro completa quando o broker confirmar (ou falhar).
     * A CHAVE é o número do pedido: todos os eventos de um pedido vão para a mesma partição
     * e são consumidos na ordem em que foram publicados.
     */
    public CompletableFuture<SendResult<String, String>> publicar(EventoDePedido evento) {
        return kafka.send(TopicosDePedidos.EVENTOS, evento.numeroPedido(), json.writeValueAsString(evento));
    }

    /**
     * Síncrono: espera a confirmação do broker. Use quando o próximo passo depende dela.
     * O envio falha de dois jeitos: o send() lança na hora (KafkaException, quando nem os metadados
     * do tópico chegaram dentro de max.block.ms) ou o futuro completa com erro (o broker recusou
     * ou não respondeu dentro de delivery.timeout.ms).
     */
    public RecordMetadata publicarEConfirmar(EventoDePedido evento) {
        try {
            return publicar(evento)
                    .get(PRAZO_DE_CONFIRMACAO.toMillis(), TimeUnit.MILLISECONDS)
                    .getRecordMetadata();
        } catch (KafkaException e) {
            throw new FalhaAoPublicarException(evento, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FalhaAoPublicarException(evento, e);
        } catch (ExecutionException e) {
            throw new FalhaAoPublicarException(evento, e.getCause());
        } catch (TimeoutException e) {
            throw new FalhaAoPublicarException(evento, e);
        }
    }
}
