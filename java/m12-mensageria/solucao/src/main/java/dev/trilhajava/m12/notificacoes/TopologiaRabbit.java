package dev.trilhajava.m12.notificacoes;

import java.util.List;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Toda a topologia declarada em código: o RabbitAdmin do Spring cria exchanges, filas e bindings
 * na primeira conexão (e não faz nada se já existirem com os mesmos argumentos).
 */
@Configuration
class TopologiaRabbit {

    static final String EXCHANGE = "notificacoes";
    static final String DLX = "notificacoes.dlx";
    static final String FILA_EMAIL = "notificacoes.email";
    static final String FILA_SMS = "notificacoes.sms";
    static final String FILA_PUSH = "notificacoes.push";
    static final String FILA_URGENTES = "notificacoes.urgentes";
    static final String FILA_AUDITORIA = "notificacoes.auditoria";
    static final String FILA_DLQ = "notificacoes.dlq";

    @Bean
    Declarables topologia() {
        TopicExchange notificacoes = new TopicExchange(EXCHANGE); // durável, não se apaga sozinha
        FanoutExchange mortas = new FanoutExchange(DLX);

        Queue email = filaDeCanal(FILA_EMAIL);
        Queue sms = filaDeCanal(FILA_SMS);
        Queue push = filaDeCanal(FILA_PUSH);
        Queue urgentes = QueueBuilder.durable(FILA_URGENTES).build();
        Queue auditoria = QueueBuilder.durable(FILA_AUDITORIA).build();
        Queue dlq = QueueBuilder.durable(FILA_DLQ).build();

        return new Declarables(
                notificacoes, mortas, email, sms, push, urgentes, auditoria, dlq,
                // "*" casa exatamente uma palavra; "#" casa zero ou mais.
                BindingBuilder.bind(email).to(notificacoes).with("email.*"),
                BindingBuilder.bind(sms).to(notificacoes).with("sms.*"),
                BindingBuilder.bind(push).to(notificacoes).with("push.*"),
                BindingBuilder.bind(urgentes).to(notificacoes).with("*.alta"),
                BindingBuilder.bind(auditoria).to(notificacoes).with("#"),
                BindingBuilder.bind(dlq).to(mortas));
    }

    /** As filas de canal são quorum queues (replicadas, o padrão recomendado no RabbitMQ 4) com DLX. */
    private static Queue filaDeCanal(String nome) {
        return QueueBuilder.durable(nome)
                .quorum()
                .deadLetterExchange(DLX)
                .build();
    }

    /** JSON no corpo, com content-type application/json. O mesmo conversor serve para publicar e consumir. */
    @Bean
    MessageConverter conversorJson(JsonMapper json) {
        return new JacksonJsonMessageConverter(json);
    }

    /**
     * O retry do listener (ligado em notificacoes.properties) não deve repetir falhas permanentes:
     * um destinatário inválido vai direto para a DLQ. A verificação percorre as causas da exceção.
     */
    @Bean
    RabbitListenerRetrySettingsCustomizer naoRepetirFalhasPermanentes() {
        return configuracao -> configuracao.setExceptionExcludes(List.of(DestinatarioInvalidoException.class));
    }
}
