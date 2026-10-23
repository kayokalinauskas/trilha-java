package dev.trilhajava.m12.aulas.producao;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

/**
 * Calcula o lag de um consumer group, partição por partição, com a API de administração:
 * lag = último offset da partição (log-end offset) − offset confirmado pelo grupo.
 * É a mesma conta que o kafka-consumer-groups.sh --describe e os exportadores de métricas fazem.
 */
public final class MonitorDeLag {

    private final Admin admin;

    public MonitorDeLag(Admin admin) {
        this.admin = admin;
    }

    public Map<TopicPartition, Long> lagPorParticao(String grupo) throws InterruptedException {
        try {
            Map<TopicPartition, OffsetAndMetadata> confirmados = admin
                    .listConsumerGroupOffsets(grupo)
                    .partitionsToOffsetAndMetadata()
                    .get();

            Map<TopicPartition, OffsetSpec> pedido = confirmados.keySet().stream()
                    .collect(Collectors.toMap(tp -> tp, tp -> OffsetSpec.latest()));
            Map<TopicPartition, ListOffsetsResultInfo> finais = admin.listOffsets(pedido).all().get();

            Map<TopicPartition, Long> lag = new TreeMap<>(
                    (a, b) -> a.toString().compareTo(b.toString()));
            confirmados.forEach((tp, confirmado) ->
                    lag.put(tp, finais.get(tp).offset() - confirmado.offset()));
            return lag;
        } catch (ExecutionException e) {
            throw new IllegalStateException("Não foi possível consultar o lag do grupo " + grupo, e.getCause());
        }
    }

    public long lagTotal(String grupo) throws InterruptedException {
        return lagPorParticao(grupo).values().stream().mapToLong(Long::longValue).sum();
    }
}
