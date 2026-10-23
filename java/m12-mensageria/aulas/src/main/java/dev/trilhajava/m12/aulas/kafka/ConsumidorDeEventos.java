package dev.trilhajava.m12.aulas.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Lê os eventos e atualiza o rastreio. Não trata exceções: se o método lançar, o error handler
 * do container decide o que fazer (repetir, mandar para um tópico de mensagens mortas). O offset
 * só é confirmado depois que o método retorna sem erro.
 */
@Component
class ConsumidorDeEventos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorDeEventos.class);

    private final RastreioDePedidos rastreio;
    private final JsonMapper json;

    ConsumidorDeEventos(RastreioDePedidos rastreio, JsonMapper json) {
        this.rastreio = rastreio;
        this.json = json;
    }

    /** concurrency = 3: três consumidores no grupo, um por partição. */
    @KafkaListener(topics = TopicosDePedidos.EVENTOS, groupId = "rastreio-pedidos", concurrency = "3")
    void receber(ConsumerRecord<String, String> registro) {
        var evento = json.readValue(registro.value(), EventoDePedido.class);
        var resultado = rastreio.aplicar(evento);
        log.info("p{}@{} chave={} {} -> {}",
                registro.partition(), registro.offset(), registro.key(), evento.status(), resultado);
    }
}
