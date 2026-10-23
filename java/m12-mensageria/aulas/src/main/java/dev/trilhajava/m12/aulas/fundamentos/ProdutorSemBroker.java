package dev.trilhajava.m12.aulas.fundamentos;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * "Quando dá errado": o broker também é uma dependência. Este produtor Kafka de verdade
 * tenta publicar num endereço onde não há broker e mostra o que o cliente faz.
 */
public class ProdutorSemBroker {

    public static void main(String[] args) throws InterruptedException {
        Map<String, Object> config = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:59092", // nenhum broker escutando aqui
                ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000,                    // o padrão é 60000 (1 minuto)
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        long inicio = System.nanoTime();
        try (var produtor = new KafkaProducer<String, String>(config)) {
            produtor.send(new ProducerRecord<>("pedidos.pagos", "PED-1002", "{\"pedidoId\":\"PED-1002\"}")).get();
            System.out.println("publicado");
        } catch (ExecutionException e) {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            System.out.println("falhou depois de " + ms + " ms: " + e.getCause());
        }
    }
}
