package dev.trilhajava.m12.estoque;

import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Publica cada movimento como JSON, com o SKU como chave. A chave decide a partição
 * (hash da chave % partições), e o Kafka só garante ordem dentro de uma partição: com a mesma
 * chave, todos os movimentos de um SKU são consumidos na ordem em que foram publicados.
 */
@Component
public class PublicadorDeMovimentos {

    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;

    public PublicadorDeMovimentos(KafkaTemplate<String, String> kafka, JsonMapper json) {
        this.kafka = kafka;
        this.json = json;
    }

    public CompletableFuture<SendResult<String, String>> publicar(MovimentoDeEstoque movimento) {
        return kafka.send(ConfiguracaoKafka.TOPICO, movimento.sku(), json.writeValueAsString(movimento));
    }
}
