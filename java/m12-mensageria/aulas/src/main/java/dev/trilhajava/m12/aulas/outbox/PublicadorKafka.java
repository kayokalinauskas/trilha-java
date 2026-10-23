package dev.trilhajava.m12.aulas.outbox;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

/** O adaptador para o Kafka: chave = agregado, id e tipo nos headers, e ESPERA a confirmação. */
public class PublicadorKafka implements Publicador {

    private final KafkaTemplate<String, String> kafka;
    private final String topico;
    private final Duration espera;

    public PublicadorKafka(KafkaTemplate<String, String> kafka, String topico, Duration espera) {
        this.kafka = kafka;
        this.topico = topico;
        this.espera = espera;
    }

    @Override
    public void publicar(Mensagem mensagem) {
        var registro = new ProducerRecord<String, String>(topico, mensagem.chave(), mensagem.payload());
        registro.headers().add("evento-id", mensagem.id().toString().getBytes(UTF_8));
        registro.headers().add("evento-tipo", mensagem.tipo().getBytes(UTF_8));
        try {
            kafka.send(registro).get(espera.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrompido) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicação interrompida: " + mensagem.id(), interrompido);
        } catch (ExecutionException | TimeoutException falha) {
            throw new IllegalStateException("Kafka não confirmou o evento " + mensagem.id(), falha);
        }
    }
}
