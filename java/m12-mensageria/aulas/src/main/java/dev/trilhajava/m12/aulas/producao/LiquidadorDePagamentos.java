package dev.trilhajava.m12.aulas.producao;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;

/**
 * Lê pagamentos autorizados, liquida cada um e publica o resultado, tudo numa transação Kafka:
 * as mensagens de saída e o avanço do offset de entrada são confirmados juntos, ou nenhum dos dois.
 * É o padrão read-process-write, o único em que o Kafka oferece exactly-once de ponta a ponta.
 */
public final class LiquidadorDePagamentos {

    public static final String ENTRADA = "pagamentos.autorizados";
    public static final String SAIDA = "pagamentos.liquidados";

    /** A regra de negócio. Pode lançar exceção: o lote inteiro é abortado e relido. */
    @FunctionalInterface
    public interface RegraDeLiquidacao {
        String liquidar(String pagamento);
    }

    private final Consumer<String, String> consumidor;
    private final Producer<String, String> produtor;
    private final RegraDeLiquidacao regra;

    public LiquidadorDePagamentos(Consumer<String, String> consumidor,
                                  Producer<String, String> produtor,
                                  RegraDeLiquidacao regra) {
        this.consumidor = consumidor;
        this.produtor = produtor;
        this.regra = regra;
        // Registra o transactional.id no coordenador e cerca ("fence") instâncias antigas com o mesmo id.
        produtor.initTransactions();
        consumidor.subscribe(List.of(ENTRADA));
    }

    /** Processa o que um poll() trouxer. Devolve quantos pagamentos foram confirmados. */
    public int processarLote(Duration espera) {
        ConsumerRecords<String, String> lote = consumidor.poll(espera);
        if (lote.isEmpty()) {
            return 0;
        }
        produtor.beginTransaction();
        try {
            for (ConsumerRecord<String, String> registro : lote) {
                String liquidado = regra.liquidar(registro.value());
                produtor.send(new ProducerRecord<>(SAIDA, registro.key(), liquidado));
            }
            // O offset de entrada vai DENTRO da transação, e não por consumidor.commitSync().
            produtor.sendOffsetsToTransaction(proximosOffsets(lote), consumidor.groupMetadata());
            produtor.commitTransaction();
            return lote.count();
        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException fatal) {
            // Outra instância assumiu este transactional.id (ou o produtor ficou inválido):
            // não dá para abortar; a única saída é fechar o produtor e deixar a instância cair.
            produtor.close();
            throw new IllegalStateException("Produtor transacional inutilizável; encerre esta instância", fatal);
        } catch (RuntimeException falha) {
            produtor.abortTransaction();
            voltarAoUltimoCommit(lote.partitions());
            throw new LoteAbortadoException(lote.count(), falha);
        }
    }

    private static Map<TopicPartition, OffsetAndMetadata> proximosOffsets(ConsumerRecords<String, String> lote) {
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        for (TopicPartition particao : lote.partitions()) {
            List<ConsumerRecord<String, String>> registros = lote.records(particao);
            long ultimo = registros.getLast().offset();
            offsets.put(particao, new OffsetAndMetadata(ultimo + 1));   // o PRÓXIMO a ler
        }
        return offsets;
    }

    /** Depois de abortar, a posição em memória do consumidor está à frente do commit: volte. */
    private void voltarAoUltimoCommit(Set<TopicPartition> particoes) {
        Map<TopicPartition, OffsetAndMetadata> confirmados = consumidor.committed(particoes);
        for (TopicPartition particao : particoes) {
            OffsetAndMetadata confirmado = confirmados.get(particao);
            if (confirmado == null) {
                consumidor.seekToBeginning(List.of(particao));
            } else {
                consumidor.seek(particao, confirmado.offset());
            }
        }
    }

    /** Sinaliza que um lote foi abortado e será relido no próximo poll(). */
    public static final class LoteAbortadoException extends RuntimeException {
        public LoteAbortadoException(int tamanho, Throwable causa) {
            super("Lote de " + tamanho + " pagamento(s) abortado; será reprocessado", causa);
        }
    }
}
