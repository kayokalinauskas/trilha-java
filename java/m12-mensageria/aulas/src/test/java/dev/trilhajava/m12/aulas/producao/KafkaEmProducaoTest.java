package dev.trilhajava.m12.aulas.producao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import dev.trilhajava.m12.aulas.producao.LiquidadorDePagamentos.LoteAbortadoException;

@EmbeddedKafka(
        partitions = 3,
        topics = {"estoque.movimentos", LiquidadorDePagamentos.ENTRADA, LiquidadorDePagamentos.SAIDA},
        brokerProperties = {
                "transaction.state.log.replication.factor=1",
                "transaction.state.log.min.isr=1"
        })
class KafkaEmProducaoTest {

    @Test
    void lagMostraAtrasoConcentradoNaParticaoDaChaveQuente(EmbeddedKafkaBroker broker) throws Exception {
        String bootstrap = broker.getBrokersAsString();
        try (var produtor = new KafkaProducer<String, String>(ConfiguracaoDeProducao.produtorDuravel(bootstrap))) {
            for (int sku = 1; sku <= 10; sku++) {
                produtor.send(new ProducerRecord<>("estoque.movimentos", "SKU-" + sku, "entrada;1"));
            }
            produtor.flush();
        }

        // O painel lê tudo e confirma os offsets.
        var config = ConfiguracaoDeProducao.consumidor(bootstrap, "painel-estoque", "painel-1");
        try (var consumidor = new KafkaConsumer<String, String>(config)) {
            consumidor.subscribe(List.of("estoque.movimentos"));
            int lidos = 0;
            Instant limite = Instant.now().plusSeconds(30);
            while (lidos < 10 && Instant.now().isBefore(limite)) {
                lidos += consumidor.poll(Duration.ofMillis(200)).count();
            }
            assertThat(lidos).isEqualTo(10);
            consumidor.commitSync();
        }

        // Uma promoção do SKU-7 gera 5 movimentos seguidos, todos com a mesma chave.
        try (var produtor = new KafkaProducer<String, String>(ConfiguracaoDeProducao.produtorDuravel(bootstrap))) {
            for (int i = 0; i < 5; i++) {
                produtor.send(new ProducerRecord<>("estoque.movimentos", "SKU-7", "saida;1"));
            }
            produtor.flush();
        }

        try (Admin admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap))) {
            var monitor = new MonitorDeLag(admin);
            Map<TopicPartition, Long> lag = monitor.lagPorParticao("painel-estoque");
            System.out.println("lag por partição: " + lag);

            assertThat(monitor.lagTotal("painel-estoque")).isEqualTo(5);
            int particaoQuente = Particionamento.particaoDa("SKU-7", 3);
            assertThat(lag).containsEntry(new TopicPartition("estoque.movimentos", particaoQuente), 5L);
        }
    }

    @Test
    void loteAbortadoNaoApareceParaQuemLeComReadCommitted(EmbeddedKafkaBroker broker) throws Exception {
        String bootstrap = broker.getBrokersAsString();
        try (var produtor = new KafkaProducer<String, String>(ConfiguracaoDeProducao.produtorDuravel(bootstrap))) {
            for (String pagamento : List.of("PAG-1;150.00", "PAG-2;89.90", "PAG-3;1200.00")) {
                produtor.send(new ProducerRecord<>(LiquidadorDePagamentos.ENTRADA, "conta-7", pagamento));
            }
            produtor.flush();
        }

        // O razão contábil dá timeout na primeira vez que vê o PAG-3 (falha transitória).
        AtomicBoolean jaFalhou = new AtomicBoolean(false);
        LiquidadorDePagamentos.RegraDeLiquidacao regra = pagamento -> {
            if (pagamento.startsWith("PAG-3") && !jaFalhou.getAndSet(true)) {
                throw new IllegalStateException("timeout no razão contábil");
            }
            return pagamento + ";LIQUIDADO";
        };

        int confirmados = 0;
        int abortados = 0;
        var configConsumidor = ConfiguracaoDeProducao.consumidor(bootstrap, "liquidacao", "liquidacao-1");
        var configProdutor = ConfiguracaoDeProducao.produtorTransacional(bootstrap, "liquidacao-1");
        try (var consumidor = new KafkaConsumer<String, String>(configConsumidor);
             var produtor = new KafkaProducer<String, String>(configProdutor)) {
            var liquidador = new LiquidadorDePagamentos(consumidor, produtor, regra);
            Instant limite = Instant.now().plusSeconds(30);
            while (confirmados < 3 && Instant.now().isBefore(limite)) {
                try {
                    confirmados += liquidador.processarLote(Duration.ofMillis(200));
                } catch (LoteAbortadoException e) {
                    System.out.println(e.getMessage() + " (causa: " + e.getCause().getMessage() + ")");
                    abortados++;
                }
            }
        }

        List<String> comprometidos = lerTudo(bootstrap, "read_committed");
        List<String> tudoQueFoiGravado = lerTudo(bootstrap, "read_uncommitted");
        System.out.println("read_committed:   " + comprometidos);
        System.out.println("read_uncommitted: " + tudoQueFoiGravado);

        assertThat(abortados).isEqualTo(1);
        assertThat(comprometidos).containsExactly(
                "PAG-1;150.00;LIQUIDADO", "PAG-2;89.90;LIQUIDADO", "PAG-3;1200.00;LIQUIDADO");
        // Se PAG-1 e PAG-2 da tentativa abortada chegaram ao broker antes do abort (depende do
        // linger.ms e da carga), eles estão no log marcados como abortados e só read_uncommitted os vê.
        // Se ainda estavam no buffer do produtor, o abort os descartou.
        assertThat(tudoQueFoiGravado).containsAll(comprometidos).hasSizeBetween(3, 5);
    }

    private static List<String> lerTudo(String bootstrap, String isolamento) {
        Map<String, Object> config = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolamento);
        List<String> valores = new ArrayList<>();
        try (var leitor = new KafkaConsumer<String, String>(config)) {
            List<TopicPartition> particoes = IntStream.range(0, 3)
                    .mapToObj(p -> new TopicPartition(LiquidadorDePagamentos.SAIDA, p))
                    .toList();
            leitor.assign(particoes);
            leitor.seekToBeginning(particoes);
            Instant limite = Instant.now().plusSeconds(3);
            while (Instant.now().isBefore(limite)) {
                leitor.poll(Duration.ofMillis(200)).forEach(registro -> valores.add(registro.value()));
            }
        }
        return valores;
    }
}
