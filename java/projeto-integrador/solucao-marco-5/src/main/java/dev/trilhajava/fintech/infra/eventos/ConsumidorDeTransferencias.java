package dev.trilhajava.fintech.infra.eventos;

import dev.trilhajava.fintech.aplicacao.ServicoDeNotificacoes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Adaptador de entrada: traduz a mensagem do Kafka numa chamada ao caso de uso. */
@Component
@ConditionalOnProperty(name = "fintech.eventos.habilitados", havingValue = "true", matchIfMissing = true)
class ConsumidorDeTransferencias {

    private final ServicoDeNotificacoes notificacoes;
    private final JsonMapper json;

    ConsumidorDeTransferencias(ServicoDeNotificacoes notificacoes, JsonMapper json) {
        this.notificacoes = notificacoes;
        this.json = json;
    }

    @KafkaListener(topics = Eventos.TOPICO, groupId = Eventos.GRUPO_NOTIFICACOES)
    void receber(String mensagem) {
        EventoJson evento = json.readValue(mensagem, EventoJson.class);
        // Leitor tolerante: um tipo de evento que este consumidor não conhece é ignorado, não é erro.
        if ("TransferenciaRealizada".equals(evento.tipo())) {
            notificacoes.registrar(evento.id(), evento.transferenciaRealizada());
        }
    }
}
