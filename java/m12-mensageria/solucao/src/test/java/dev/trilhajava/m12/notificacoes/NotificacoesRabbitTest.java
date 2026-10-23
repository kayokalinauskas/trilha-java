package dev.trilhajava.m12.notificacoes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jayway.jsonpath.JsonPath;
import dev.trilhajava.m12.ContainersDeMensageria;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

@SpringBootTest(classes = NotificacoesApplication.class)
@Import(ContainersDeMensageria.Rabbit.class)
@EnabledIfDockerAvailable
@DisplayName("Exercício 12.01 — roteamento de notificações com RabbitMQ")
class NotificacoesRabbitTest {

    static final List<String> FILAS = List.of("notificacoes.email", "notificacoes.sms", "notificacoes.push",
            "notificacoes.urgentes", "notificacoes.auditoria", "notificacoes.dlq");

    static final long ESPERA = 5_000;

    @Autowired
    PublicadorDeNotificacoes publicador;

    @Autowired
    RabbitTemplate rabbit;

    @Autowired
    AmqpAdmin admin;

    @MockitoBean
    EnviadorDeEmail enviador;

    @BeforeEach
    void esvaziarFilas() {
        FILAS.forEach(fila -> {
            if (admin.getQueueInfo(fila) != null) {
                admin.purgeQueue(fila, false);
            }
        });
    }

    Message receber(String fila) {
        return rabbit.receive(fila, ESPERA);
    }

    String json(Message mensagem) {
        return new String(mensagem.getBody(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("declara as seis filas")
    void topologia() {
        for (String fila : FILAS) {
            assertThat(admin.getQueueInfo(fila)).as("fila %s", fila).isNotNull();
            assertThat(admin.getQueueProperties(fila)).as("fila %s", fila).isNotNull();
        }
    }

    @Test
    @DisplayName("sms.normal chega em sms e auditoria, e em nenhuma outra")
    void smsNormal() {
        var n = Notificacao.nova(Canal.SMS, Prioridade.NORMAL, "+5511999990000", "Seu pedido saiu para entrega");
        publicador.publicar(n);

        assertThat(receber("notificacoes.sms")).isNotNull();
        assertThat(receber("notificacoes.auditoria")).isNotNull();
        assertThat(rabbit.receive("notificacoes.urgentes")).isNull();
        assertThat(rabbit.receive("notificacoes.push")).isNull();
        assertThat(rabbit.receive("notificacoes.dlq")).isNull();
    }

    @Test
    @DisplayName("push.alta chega em push, urgentes e auditoria")
    void pushAlta() {
        publicador.publicar(Notificacao.nova(Canal.PUSH, Prioridade.ALTA, "dispositivo-42", "Login de um novo aparelho"));

        assertThat(receber("notificacoes.push")).isNotNull();
        assertThat(receber("notificacoes.urgentes")).isNotNull();
        assertThat(receber("notificacoes.auditoria")).isNotNull();
        assertThat(rabbit.receive("notificacoes.sms")).isNull();
    }

    @Test
    @DisplayName("push.baixa não chega em urgentes")
    void pushBaixa() {
        publicador.publicar(Notificacao.nova(Canal.PUSH, Prioridade.BAIXA, "dispositivo-42", "Novidades da semana"));

        assertThat(receber("notificacoes.push")).isNotNull();
        assertThat(receber("notificacoes.auditoria")).isNotNull();
        assertThat(rabbit.receive("notificacoes.urgentes")).isNull();
    }

    @Test
    @DisplayName("a mensagem é JSON, persistente e leva o id no message_id")
    void formato() {
        var n = Notificacao.nova(Canal.SMS, Prioridade.ALTA, "+5511988887777", "Código: 123456");
        publicador.publicar(n);

        Message m = receber("notificacoes.sms");
        assertThat(m).isNotNull();
        assertThat(m.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(m.getMessageProperties().getMessageId()).isEqualTo(n.id().toString());
        assertThat(m.getMessageProperties().getReceivedDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(m.getMessageProperties().getReceivedRoutingKey()).isEqualTo("sms.alta");
        String corpo = json(m);
        assertThat(JsonPath.<String>read(corpo, "$.id")).isEqualTo(n.id().toString());
        assertThat(JsonPath.<String>read(corpo, "$.canal")).isEqualTo("SMS");
        assertThat(JsonPath.<String>read(corpo, "$.destinatario")).isEqualTo("+5511988887777");
        assertThat(JsonPath.<String>read(corpo, "$.mensagem")).isEqualTo("Código: 123456");
    }

    @Test
    @DisplayName("email.alta: o consumidor envia o e-mail; a cópia vai para urgentes e auditoria")
    void emailAlta() {
        publicador.publicar(Notificacao.nova(Canal.EMAIL, Prioridade.ALTA, "ana@exemplo.com", "Sua senha foi alterada"));

        verify(enviador, timeout(ESPERA)).enviar("ana@exemplo.com", "Sua senha foi alterada");
        assertThat(receber("notificacoes.urgentes")).isNotNull();
        assertThat(receber("notificacoes.auditoria")).isNotNull();
        assertThat(rabbit.receive("notificacoes.sms")).isNull();
    }

    @Test
    @DisplayName("falha transitória: tenta de novo e entrega (3 chamadas, nada na DLQ)")
    void falhaTransitoria() {
        doThrow(new ProvedorIndisponivelException("503"))
                .doThrow(new ProvedorIndisponivelException("timeout"))
                .doNothing()
                .when(enviador).enviar(anyString(), anyString());

        publicador.publicar(Notificacao.nova(Canal.EMAIL, Prioridade.NORMAL, "bia@exemplo.com", "Fatura disponível"));

        verify(enviador, timeout(ESPERA).times(3)).enviar("bia@exemplo.com", "Fatura disponível");
        assertThat(rabbit.receive("notificacoes.dlq", 1_000)).isNull();
    }

    @Test
    @DisplayName("falha que não passa: 3 tentativas (1 + 2 repetições) e a mensagem vai para a DLQ")
    void esgotaTentativas() {
        doThrow(new ProvedorIndisponivelException("fora do ar")).when(enviador).enviar(anyString(), anyString());
        var n = Notificacao.nova(Canal.EMAIL, Prioridade.NORMAL, "caio@exemplo.com", "Bem-vindo");

        publicador.publicar(n);

        Message morta = receber("notificacoes.dlq");
        assertThat(morta).as("a mensagem deveria estar na DLQ").isNotNull();
        verify(enviador, times(3)).enviar("caio@exemplo.com", "Bem-vindo");
        assertThat(morta.getMessageProperties().getMessageId()).isEqualTo(n.id().toString());
        List<Map<String, ?>> mortes = morta.getMessageProperties().getXDeathHeader();
        assertThat(mortes).isNotEmpty();
        assertThat(mortes.getFirst().get("queue")).isEqualTo("notificacoes.email");
        assertThat(mortes.getFirst().get("reason")).isEqualTo("rejected");
    }

    @Test
    @DisplayName("destinatário inválido: falha permanente, vai direto para a DLQ (sem repetir) e sem chamar o provedor")
    void destinatarioInvalido() {
        doNothing().when(enviador).enviar(anyString(), anyString());
        long inicio = System.nanoTime();

        publicador.publicar(Notificacao.nova(Canal.EMAIL, Prioridade.ALTA, "sem-arroba", "Oi"));

        assertThat(receber("notificacoes.dlq")).isNotNull();
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        // Com as 2 repetições (200 ms + 400 ms), a mensagem levaria pelo menos 600 ms para morrer.
        assertThat(ms).as("chegou à DLQ em %d ms: a falha permanente foi repetida?", ms).isLessThan(550);
        verify(enviador, after(300).never()).enviar(anyString(), anyString());
    }

    @Test
    @DisplayName("a DLQ não recebe nada quando tudo dá certo")
    void semMortes() {
        publicador.publicar(Notificacao.nova(Canal.EMAIL, Prioridade.BAIXA, "davi@exemplo.com", "Newsletter"));

        verify(enviador, timeout(ESPERA)).enviar("davi@exemplo.com", "Newsletter");
        assertThat(rabbit.receive("notificacoes.dlq", 1_000)).isNull();
    }
}
