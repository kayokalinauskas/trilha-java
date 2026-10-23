package dev.trilhajava.m12.aulas.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Retry NÃO bloqueante: a mensagem que falha sai do tópico principal e vai para
 * notas.emitidas-retry-100, -retry-200, -retry-400 e, por fim, notas.emitidas-dlt.
 * Os atrasos são curtos para a aula rodar rápido; em produção, segundos ou minutos.
 */
@Component
class EnvioDeNotaPorEmail {

    private static final Logger log = LoggerFactory.getLogger(EnvioDeNotaPorEmail.class);

    private final ProvedorDeEmail provedor;
    private final JsonMapper json;

    EnvioDeNotaPorEmail(ProvedorDeEmail provedor, JsonMapper json) {
        this.provedor = provedor;
        this.json = json;
    }

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 100, multiplier = 2.0, maxDelay = 1_000),
            numPartitions = "3",
            exclude = {MensagemInvalidaException.class, JacksonException.class},
            traversingCauses = "true")
    @KafkaListener(topics = ConfiguracaoDeRetry.NOTAS_EMITIDAS, groupId = "envio-de-notas")
    void receber(String mensagem, @Header(KafkaHeaders.RECEIVED_TOPIC) String topico) {
        NotaEmitida nota = json.readValue(mensagem, NotaEmitida.class);
        if (nota.email() == null || !nota.email().contains("@")) {
            throw new MensagemInvalidaException("e-mail inválido no pedido " + nota.numeroPedido());
        }
        log.info("enviando a nota do pedido {} (lida de {})", nota.numeroPedido(), topico);
        provedor.enviar(nota);
    }

    @DltHandler
    void naDlt(String mensagem, @Header(KafkaHeaders.RECEIVED_TOPIC) String topico) {
        log.warn("e-mail desistido, mensagem em {}: {}", topico, mensagem);
    }
}
