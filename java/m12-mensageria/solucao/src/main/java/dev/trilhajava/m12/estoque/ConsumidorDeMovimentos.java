package dev.trilhajava.m12.estoque;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Aplica cada movimento na projeção. Não trata exceções: quem decide entre repetir e mandar
 * para o DLT é o DefaultErrorHandler. Um catch aqui "engoliria" a mensagem e o offset avançaria.
 */
@Component
class ConsumidorDeMovimentos {

    private final ProjecaoDeEstoque projecao;
    private final JsonMapper json;

    ConsumidorDeMovimentos(ProjecaoDeEstoque projecao, JsonMapper json) {
        this.projecao = projecao;
        this.json = json;
    }

    /** Uma thread por partição. O JSON é lido aqui, e não no deserializer: um JSON inválido vira uma exceção comum. */
    @KafkaListener(topics = ConfiguracaoKafka.TOPICO, groupId = "projecao-estoque", concurrency = "3")
    void receber(String mensagem) {
        projecao.aplicar(json.readValue(mensagem, MovimentoDeEstoque.class));
    }
}
