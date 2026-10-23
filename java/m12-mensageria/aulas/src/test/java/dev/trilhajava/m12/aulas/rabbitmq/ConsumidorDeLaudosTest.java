package dev.trilhajava.m12.aulas.rabbitmq;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import com.rabbitmq.client.Channel;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.test.TestRabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * O @RabbitListener de verdade, com o conversor JSON de verdade, mas sem broker:
 * o TestRabbitTemplate entrega a mensagem direto ao listener da fila.
 */
@SpringJUnitConfig
class ConsumidorDeLaudosTest {

    @Autowired TestRabbitTemplate rabbit;
    @Autowired GeradorDeLaudo gerador;

    @Test
    void entregaOExameAoGerador() {
        var exame = ExameConcluido.novo("PAC-1001", TipoDeExame.IMAGEM, "sp", Instant.parse("2026-10-05T13:30:00Z"));

        rabbit.convertAndSend(TopologiaDeExames.FILA_LAUDOS, exame);

        then(gerador).should().gerar(exame);
    }

    @Test
    void falhaPermanenteViraRejeicaoSemRequeue() {
        var exame = ExameConcluido.novo("PAC-1002", TipoDeExame.SANGUE, "sp", Instant.parse("2026-10-05T13:30:00Z"));
        willThrow(new LaudoInvalidoException("sangue não tem laudo de imagem")).given(gerador).gerar(exame);

        assertThatThrownBy(() -> rabbit.convertAndSend(TopologiaDeExames.FILA_LAUDOS, exame))
                .hasRootCauseInstanceOf(LaudoInvalidoException.class)
                .satisfies(erro -> System.out.println(erro))
                .cause().isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    @Configuration
    @EnableRabbit
    @Import(ConsumidorDeLaudos.class)
    static class Config {

        @Bean
        ConnectionFactory conexoes() throws Exception {
            ConnectionFactory fabrica = mock(ConnectionFactory.class);
            Connection conexao = mock(Connection.class);
            Channel canal = mock(Channel.class);
            willReturn(conexao).given(fabrica).createConnection();
            willReturn(canal).given(conexao).createChannel(anyBoolean());
            given(canal.isOpen()).willReturn(true);
            return fabrica;
        }

        @Bean
        TestRabbitTemplate rabbit(ConnectionFactory conexoes, JacksonJsonMessageConverter json) {
            var template = new TestRabbitTemplate(conexoes);
            template.setMessageConverter(json);
            return template;
        }

        @Bean
        JacksonJsonMessageConverter json() {
            return new JacksonJsonMessageConverter();
        }

        @Bean
        SimpleRabbitListenerContainerFactory fabricaDeLaudos(ConnectionFactory conexoes, JacksonJsonMessageConverter json) {
            var fabrica = new SimpleRabbitListenerContainerFactory();
            fabrica.setConnectionFactory(conexoes);
            fabrica.setMessageConverter(json);
            return fabrica;
        }

        @Bean
        GeradorDeLaudo gerador() {
            return mock(GeradorDeLaudo.class);
        }
    }
}
