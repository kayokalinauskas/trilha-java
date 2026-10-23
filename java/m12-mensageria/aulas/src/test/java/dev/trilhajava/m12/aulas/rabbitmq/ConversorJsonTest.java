package dev.trilhajava.m12.aulas.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import tools.jackson.databind.json.JsonMapper;

/** Como o ExameConcluido vira bytes no corpo da mensagem, e volta. Sem broker. */
class ConversorJsonTest {

    private final MessageConverter conversor = new TopologiaDeExames().conversorJson(JsonMapper.builder().build());

    @Test
    void idaEVoltaEmJson() {
        var exame = new ExameConcluido(UUID.fromString("0f8e2c1a-6b1d-4a39-9d0e-2b7c5d8a9f10"),
                "PAC-1001", TipoDeExame.IMAGEM, "sp", Instant.parse("2026-10-05T13:30:00Z"));

        Message mensagem = conversor.toMessage(exame, new MessageProperties());

        String corpo = new String(mensagem.getBody(), StandardCharsets.UTF_8);
        System.out.println(mensagem.getMessageProperties().getContentType());
        System.out.println(mensagem.getMessageProperties().getHeaders());
        System.out.println(corpo);
        assertThat(mensagem.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(corpo).contains("\"tipo\":\"IMAGEM\"", "\"unidade\":\"sp\"");
        assertThat(conversor.fromMessage(mensagem)).isEqualTo(exame);
    }
}
