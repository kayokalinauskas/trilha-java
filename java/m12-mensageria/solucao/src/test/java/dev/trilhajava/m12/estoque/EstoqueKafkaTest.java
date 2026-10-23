package dev.trilhajava.m12.estoque;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m12.ContainersDeMensageria;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

@SpringBootTest(classes = {EstoqueApplication.class, EstoqueKafkaTest.Instabilidade.class})
@Import(ContainersDeMensageria.Kafka.class)
@EnabledIfDockerAvailable
@DisplayName("Exercício 12.02 — projeção de estoque com Kafka (chaves, ordem, retry e DLT)")
class EstoqueKafkaTest {

    static final String TOPICO = "estoque.movimentos";
    static final String DLT = "estoque.movimentos-dlt";

    /** Uma projeção que conta as tentativas por SKU e pode falhar de propósito (falha transitória). */
    static class ProjecaoInstavel extends ProjecaoDeEstoque {
        final Map<String, AtomicInteger> tentativas = new ConcurrentHashMap<>();
        final Map<String, Integer> falhasProgramadas = new ConcurrentHashMap<>();

        void falhar(String sku, int vezes) {
            falhasProgramadas.put(sku, vezes);
        }

        int tentativas(String sku) {
            return tentativas.getOrDefault(sku, new AtomicInteger()).get();
        }

        @Override
        public void aplicar(MovimentoDeEstoque movimento) {
            tentativas.computeIfAbsent(movimento.sku(), sku -> new AtomicInteger()).incrementAndGet();
            Integer restantes = falhasProgramadas.get(movimento.sku());
            if (restantes != null && restantes > 0) {
                falhasProgramadas.put(movimento.sku(), restantes - 1);
                throw new BancoIndisponivelException("conexão recusada");
            }
            super.aplicar(movimento);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Instabilidade {
        @Bean
        @Primary
        ProjecaoInstavel projecaoInstavel() {
            return new ProjecaoInstavel();
        }
    }

    @Autowired
    PublicadorDeMovimentos publicador;

    @Autowired
    ProjecaoInstavel projecao;

    @Autowired
    KafkaTemplate<String, String> kafka;

    @Autowired
    KafkaAdmin admin;

    @Autowired
    ConsumerFactory<?, ?> fabricaDeConsumidores;

    static String sku(String prefixo) {
        return prefixo + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    SendResult<String, String> publicar(String sku, int quantidade) throws Exception {
        return publicador.publicar(MovimentoDeEstoque.de(sku, quantidade)).get();
    }

    static void esperarAte(BooleanSupplier condicao, String descricao) throws InterruptedException {
        long limite = System.nanoTime() + Duration.ofSeconds(15).toNanos();
        while (!condicao.getAsBoolean()) {
            if (System.nanoTime() > limite) {
                throw new AssertionError("não aconteceu em 15 s: " + descricao);
            }
            Thread.sleep(50);
        }
    }

    /** Lê o DLT com um grupo novo (do início) e devolve as mensagens com a chave pedida. */
    @SuppressWarnings("unchecked")
    List<ConsumerRecord<String, String>> mortas(String chave, Duration espera) {
        List<ConsumerRecord<String, String>> achadas = new ArrayList<>();
        try (Consumer<String, String> consumidor = (Consumer<String, String>) fabricaDeConsumidores
                .createConsumer("teste-dlt-" + UUID.randomUUID(), null)) {
            consumidor.subscribe(List.of(DLT));
            long limite = System.nanoTime() + espera.toNanos();
            while (System.nanoTime() < limite && achadas.isEmpty()) {
                for (ConsumerRecord<String, String> r : consumidor.poll(Duration.ofMillis(200))) {
                    if (chave.equals(r.key())) {
                        achadas.add(r);
                    }
                }
            }
        }
        return achadas;
    }

    static String cabecalho(ConsumerRecord<String, String> registro, String nome) {
        Header h = registro.headers().lastHeader(nome);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("cria o tópico e o DLT com 3 partições cada")
    void topicos() {
        Map<String, TopicDescription> topicos = admin.describeTopics(TOPICO, DLT);
        assertThat(topicos.get(TOPICO).partitions()).hasSize(3);
        assertThat(topicos.get(DLT).partitions()).hasSize(3);
    }

    @Test
    @DisplayName("o produtor declara acks=all e idempotência explicitamente")
    void produtorDuravel() {
        Map<String, Object> config = kafka.getProducerFactory().getConfigurationProperties();
        assertThat(String.valueOf(config.get("acks"))).isEqualTo("all");
        assertThat(String.valueOf(config.get("enable.idempotence"))).isEqualTo("true");
    }

    @Test
    @DisplayName("a chave é o SKU: todos os movimentos de um SKU caem na mesma partição")
    void mesmaParticao() throws Exception {
        String sku = sku("PARTICAO");
        Set<Integer> particoes = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            SendResult<String, String> r = publicar(sku, 1);
            assertThat(r.getProducerRecord().key()).isEqualTo(sku);
            particoes.add(r.getRecordMetadata().partition());
        }
        assertThat(particoes).hasSize(1);
    }

    @Test
    @DisplayName("SKUs diferentes se espalham pelas partições")
    void espalha() throws Exception {
        Set<Integer> particoes = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            particoes.add(publicar(sku("ESPALHA"), 1).getRecordMetadata().partition());
        }
        assertThat(particoes).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("o JSON publicado tem id, sku, quantidade e ocorridoEm")
    void formato() throws Exception {
        String sku = sku("JSON");
        String valor = publicar(sku, 7).getProducerRecord().value();
        assertThat(valor).contains("\"sku\":\"" + sku + "\"", "\"quantidade\":7", "\"id\":", "\"ocorridoEm\":");
    }

    @Test
    @DisplayName("a projeção aplica entradas e saídas")
    void aplica() throws Exception {
        String sku = sku("APLICA");
        publicar(sku, 10);
        publicar(sku, -3);
        esperarAte(() -> projecao.saldo(sku) == 7, "saldo de " + sku + " = 7");
    }

    @Test
    @DisplayName("ordem por chave: +10 seguido de dez -1 termina em 0, sem nada no DLT")
    void ordem() throws Exception {
        String sku = sku("ORDEM");
        publicar(sku, 10);
        for (int i = 0; i < 10; i++) {
            publicar(sku, -1);
        }
        esperarAte(() -> projecao.tentativas(sku) == 11, "11 movimentos processados");
        assertThat(projecao.saldo(sku)).isZero();
        assertThat(mortas(sku, Duration.ofSeconds(2))).isEmpty();
    }

    @Test
    @DisplayName("falha transitória: repete (3 tentativas) e aplica o movimento uma vez")
    void transitoria() throws Exception {
        String sku = sku("INSTAVEL");
        projecao.falhar(sku, 2);
        publicar(sku, 5);
        esperarAte(() -> projecao.saldo(sku) == 5, "saldo 5 depois das repetições");
        assertThat(projecao.tentativas(sku)).isEqualTo(3);
        assertThat(mortas(sku, Duration.ofSeconds(2))).isEmpty();
    }

    @Test
    @DisplayName("falha que não passa: 3 tentativas e DLT, com o tópico de origem e a exceção nos cabeçalhos")
    void esgota() throws Exception {
        String sku = sku("FORA-DO-AR");
        projecao.falhar(sku, 99);
        publicar(sku, 5);

        List<ConsumerRecord<String, String>> mortas = mortas(sku, Duration.ofSeconds(15));
        assertThat(mortas).hasSize(1);
        assertThat(projecao.tentativas(sku)).isEqualTo(3);
        assertThat(projecao.saldo(sku)).isZero();
        ConsumerRecord<String, String> morta = mortas.getFirst();
        assertThat(morta.value()).contains(sku);
        assertThat(cabecalho(morta, KafkaHeaders.DLT_ORIGINAL_TOPIC)).isEqualTo(TOPICO);
        assertThat(cabecalho(morta, KafkaHeaders.DLT_EXCEPTION_FQCN) + " " + cabecalho(morta, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN))
                .contains(BancoIndisponivelException.class.getName());
    }

    @Test
    @DisplayName("saldo negativo é falha permanente: 1 tentativa só, e vai para o DLT")
    void permanente() throws Exception {
        String sku = sku("NEGATIVO");
        publicar(sku, -5);

        assertThat(mortas(sku, Duration.ofSeconds(15))).hasSize(1);
        assertThat(projecao.tentativas(sku)).isEqualTo(1);
    }

    @Test
    @DisplayName("mensagem envenenada (JSON inválido) vai para o DLT sem travar a partição")
    void envenenada() throws Exception {
        String sku = sku("VENENO");
        kafka.send(TOPICO, sku, "{isto não é json").get();
        publicar(sku, 3);

        esperarAte(() -> projecao.saldo(sku) == 3, "o movimento seguinte ao veneno ser aplicado");
        List<ConsumerRecord<String, String>> mortas = mortas(sku, Duration.ofSeconds(15));
        assertThat(mortas).hasSize(1);
        assertThat(mortas.getFirst().value()).isEqualTo("{isto não é json");
    }

    @Test
    @DisplayName("movimento inválido (quantidade zero) vai para o DLT sem chegar à projeção")
    void invalido() throws Exception {
        String sku = sku("ZERO");
        String json = """
                {"id": "%s", "sku": "%s", "quantidade": 0, "ocorridoEm": "2026-09-29T12:00:00Z"}"""
                .formatted(UUID.randomUUID(), sku);
        kafka.send(TOPICO, sku, json).get();

        assertThat(mortas(sku, Duration.ofSeconds(15))).hasSize(1);
        assertThat(projecao.tentativas(sku)).isZero();
    }
}
