package dev.trilhajava.m12.aulas.retry;

import static dev.trilhajava.m12.aulas.retry.ConfiguracaoDeRetry.NOTAS_EMITIDAS;
import static dev.trilhajava.m12.aulas.retry.ConfiguracaoDeRetry.PEDIDOS_PAGOS;
import static dev.trilhajava.m12.aulas.retry.ConfiguracaoDeRetry.PEDIDOS_PAGOS_DLT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

/** Um broker Kafka de verdade, dentro da JVM do teste (sem Docker). */
@SpringBootTest(properties = "logging.level.dev.trilhajava.m12.aulas.retry=INFO")
@EmbeddedKafka(partitions = 3, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class RetryEDlqTest {

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired EmissorDeNotasSimulado emissor;
    @Autowired ProvedorDeEmailSimulado provedorDeEmail;
    @Autowired ReprocessadorDaDlt reprocessador;
    @Autowired EmbeddedKafkaBroker broker;

    @Test
    void falhaTransitoriaSeResolveNoRetry() {
        emissor.falharAsProximas(2);
        int chamadasAntes = emissor.chamadas();

        kafka.send(PEDIDOS_PAGOS, "PED-1", """
                {"numeroPedido":"PED-1","clienteId":"cli-7","valor":149.90}""");

        await().atMost(Duration.ofSeconds(15))
                .until(() -> emissor.notasEmitidas().containsKey("PED-1"));
        assertThat(emissor.chamadas() - chamadasAntes).isEqualTo(3); // 2 falhas + 1 sucesso
    }

    @Test
    void falhaPermanenteVaiDiretoParaODltComOsHeadersDeDiagnostico() {
        int chamadasAntes = emissor.chamadas();

        kafka.send(PEDIDOS_PAGOS, "PED-2", "{\"numeroPedido\":\"PED-2\", valor: quebrado");

        ConsumerRecord<String, String> morta = lerDoDlt(PEDIDOS_PAGOS_DLT, "PED-2");
        assertThat(morta.value()).isEqualTo("{\"numeroPedido\":\"PED-2\", valor: quebrado");
        assertThat(ReprocessadorDaDlt.texto(morta, KafkaHeaders.DLT_ORIGINAL_TOPIC)).isEqualTo(PEDIDOS_PAGOS);
        assertThat(ReprocessadorDaDlt.texto(morta, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN))
                .isEqualTo("tools.jackson.core.exc.StreamReadException");
        assertThat(emissor.chamadas()).isEqualTo(chamadasAntes); // nem chegou ao emissor
    }

    @Test
    void depoisDaCorrecaoOReprocessamentoRecuperaAMensagem() {
        emissor.ficarForaDoAr(true);
        kafka.send(PEDIDOS_PAGOS, "PED-3", """
                {"numeroPedido":"PED-3","clienteId":"cli-9","valor":89.00}""");
        lerDoDlt(PEDIDOS_PAGOS_DLT, "PED-3"); // 4 tentativas (~1,4 s de espera) e foi para o DLT

        emissor.ficarForaDoAr(false); // "o emissor voltou"
        var relatorio = reprocessador.reprocessar(PEDIDOS_PAGOS_DLT,
                morta -> "PED-3".equals(morta.key())
                        && ServicoIndisponivelException.class.getName()
                                .equals(ReprocessadorDaDlt.texto(morta, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN)),
                100);

        assertThat(relatorio.republicadas()).isEqualTo(1);
        await().atMost(Duration.ofSeconds(15))
                .until(() -> emissor.notasEmitidas().containsKey("PED-3"));
    }

    @Test
    void topicosDeRetryNaoBloqueiamAsMensagensSeguintesMasPerdemAOrdem() {
        provedorDeEmail.falharParaOPedido("PED-10", 2);

        // mesma chave = mesma partição: no retry bloqueante, PED-11 esperaria PED-10
        kafka.send(NOTAS_EMITIDAS, "cli-7", """
                {"numeroPedido":"PED-10","email":"ana@exemplo.com.br","chaveDaNota":"NFE-PED-10"}""");
        kafka.send(NOTAS_EMITIDAS, "cli-7", """
                {"numeroPedido":"PED-11","email":"ana@exemplo.com.br","chaveDaNota":"NFE-PED-11"}""");

        await().atMost(Duration.ofSeconds(20))
                .until(() -> provedorDeEmail.pedidosEnviados().contains("PED-10"));
        assertThat(provedorDeEmail.pedidosEnviados()).containsSubsequence("PED-11", "PED-10");
    }

    private ConsumerRecord<String, String> lerDoDlt(String topico, String chave) {
        var props = KafkaTestUtils.consumerProps(broker, "leitor-" + chave, false);
        var fabrica = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());
        try (var consumidor = fabrica.createConsumer()) {
            consumidor.subscribe(List.of(topico));
            List<ConsumerRecord<String, String>> encontradas = new ArrayList<>();
            await().atMost(Duration.ofSeconds(15)).until(() -> {
                consumidor.poll(Duration.ofMillis(500))
                        .forEach(r -> { if (chave.equals(r.key())) encontradas.add(r); });
                return !encontradas.isEmpty();
            });
            return encontradas.getFirst();
        }
    }
}
