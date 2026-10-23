package dev.trilhajava.m12.aulas.producao;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * As configurações de cliente que um time costuma fixar por escrito antes de ir para produção.
 * Cada valor está aqui de propósito, mesmo quando coincide com o padrão do Kafka 4:
 * explícito no código, ele sobrevive a uma mudança de padrão numa versão futura e
 * aparece na revisão de código.
 */
public final class ConfiguracaoDeProducao {

    private ConfiguracaoDeProducao() {
    }

    /** Produtor para eventos que não podem se perder (pagamentos, pedidos). */
    public static Map<String, Object> produtorDuravel(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Durabilidade: o líder só confirma depois que todas as réplicas em sincronia gravaram.
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // Reenvio sem duplicar: o broker descarta a cópia repetida pelo número de sequência.
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // Vazão: espera até 20 ms para encher lotes de até 64 KiB por partição.
        props.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 64 * 1024);
        // Compressão por lote: menos rede e menos disco nos brokers, um pouco mais de CPU.
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
        // Limite total para um envio dar certo ou falhar (inclui os retries).
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000);
        return props;
    }

    /** Produtor transacional: o mesmo produtor durável, com um transactional.id estável por instância. */
    public static Map<String, Object> produtorTransacional(String bootstrapServers, String transactionalId) {
        Map<String, Object> props = produtorDuravel(bootstrapServers);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
        return props;
    }

    /**
     * Consumidor com o protocolo de rebalanceamento novo (KIP-848), membro estático e
     * leitura só do que foi confirmado em transações.
     *
     * @param instancia identificador estável da instância (ex. o nome do pod de um StatefulSet)
     */
    public static Map<String, Object> consumidor(String bootstrapServers, String grupo, String instancia) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, grupo);
        // Protocolo novo: o coordenador do grupo, no broker, calcula as atribuições.
        props.put(ConsumerConfig.GROUP_PROTOCOL_CONFIG, "consumer");
        // Membro estático: reiniciar a instância não dispara um rebalanceamento imediato.
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, instancia);
        // Ignora mensagens de transações abortadas ou ainda abertas.
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        // O offset só avança quando o código manda: nada de commit automático por tempo.
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Tempo máximo entre dois poll() antes de o membro ser considerado travado.
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 200);
        return props;
    }
}
