package dev.trilhajava.m12.aulas.rabbitmq;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * A topologia inteira em código. O RabbitAdmin (criado pelo Spring Boot) declara tudo na
 * primeira conexão; se já existir com os mesmos argumentos, não faz nada.
 */
@Configuration
public class TopologiaDeExames {

    public static final String EXCHANGE = "clinica.exames";
    public static final String DLX = "clinica.exames.dlx";
    public static final String FILA_LAUDOS = "exames.laudos-de-imagem";
    public static final String FILA_AVISOS = "exames.avisos-ao-paciente";
    public static final String FILA_PAINEL_SP = "exames.painel-sp";
    public static final String FILA_DLQ = "exames.dlq";

    @Bean
    public Declarables topologia() {
        TopicExchange exames = new TopicExchange(EXCHANGE);   // durável, não se apaga sozinha
        FanoutExchange mortas = new FanoutExchange(DLX);

        // Laudo de imagem: trabalho lento e importante. Quorum queue (replicada) com DLX
        // e no máximo 5 reentregas antes de ir para a DLQ (o padrão do RabbitMQ 4 é 20).
        Queue laudos = QueueBuilder.durable(FILA_LAUDOS)
                .quorum()
                .deliveryLimit(5)
                .deadLetterExchange(DLX)
                .build();

        // Aviso ao paciente: todo exame concluído, de qualquer tipo e unidade.
        Queue avisos = QueueBuilder.durable(FILA_AVISOS)
                .quorum()
                .deadLetterExchange(DLX)
                .build();

        // Painel da unidade de São Paulo: só interessa o recente. Fila clássica limitada:
        // 1.000 mensagens ou 1 hora; passando disso, as mais antigas são descartadas.
        Queue painelSp = QueueBuilder.durable(FILA_PAINEL_SP)
                .maxLength(1_000)
                .overflow(QueueBuilder.Overflow.dropHead)
                .ttl(3_600_000)
                .build();

        Queue dlq = QueueBuilder.durable(FILA_DLQ).quorum().build();

        return new Declarables(
                exames, mortas, laudos, avisos, painelSp, dlq,
                BindingBuilder.bind(laudos).to(exames).with("imagem.*"),   // "*": exatamente uma palavra
                BindingBuilder.bind(avisos).to(exames).with("#"),          // "#": zero ou mais palavras
                BindingBuilder.bind(painelSp).to(exames).with("*.sp"),
                BindingBuilder.bind(dlq).to(mortas));                      // fanout ignora a routing key
    }

    /**
     * JSON no corpo (content-type application/json), para publicar e para consumir. O header
     * __TypeId__ só é aceito para classes deste pacote ("pacotes confiáveis"): uma mensagem
     * forjada não consegue mandar o Jackson instanciar uma classe qualquer do classpath.
     */
    @Bean
    public MessageConverter conversorJson(JsonMapper json) {
        return new JacksonJsonMessageConverter(json, "dev.trilhajava.m12.aulas.rabbitmq");
    }

    /**
     * Fábrica de containers para o consumidor de laudos: herda tudo o que está em
     * spring.rabbitmq.listener.simple.* e muda só o prefetch. Cada laudo leva segundos, então
     * cada consumidor segura no máximo 1 mensagem não confirmada.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory fabricaDeLaudos(
            SimpleRabbitListenerContainerFactoryConfigurer configurador, ConnectionFactory conexoes) {
        var fabrica = new SimpleRabbitListenerContainerFactory();
        configurador.configure(fabrica, conexoes);
        fabrica.setPrefetchCount(1);
        return fabrica;
    }
}
