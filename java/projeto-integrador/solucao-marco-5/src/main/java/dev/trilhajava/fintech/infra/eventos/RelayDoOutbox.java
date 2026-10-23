package dev.trilhajava.fintech.infra.eventos;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Publica no Kafka os eventos pendentes da outbox. Garantia: pelo menos uma vez (se a aplicação cair
 * entre o envio e o UPDATE, o evento sai de novo — por isso os consumidores são idempotentes).
 *
 * FOR UPDATE SKIP LOCKED: com várias instâncias, cada uma pega um lote diferente em vez de esperar.
 */
@Component
@ConditionalOnProperty(name = "fintech.eventos.habilitados", havingValue = "true", matchIfMissing = true)
class RelayDoOutbox {

    private static final Logger log = LoggerFactory.getLogger(RelayDoOutbox.class);
    private static final int LOTE = 100;
    private static final Duration ESPERA_MAXIMA = Duration.ofMinutes(5);

    private record Pendente(UUID id, UUID agregado, String payload, int tentativas) {
    }

    private final JdbcClient jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final TransactionTemplate transacao;
    private final Clock clock;

    RelayDoOutbox(JdbcClient jdbc, KafkaTemplate<String, String> kafka, TransactionTemplate transacao, Clock clock) {
        this.jdbc = jdbc;
        this.kafka = kafka;
        this.transacao = transacao;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${fintech.eventos.relay.intervalo-ms:500}")
    void executar() {
        transacao.executeWithoutResult(status -> publicarLote());
    }

    private void publicarLote() {
        Timestamp agora = Timestamp.from(clock.instant());
        List<Pendente> lote = jdbc.sql("""
                        SELECT id, agregado_id, payload, tentativas FROM evento_saida
                        WHERE publicado_em IS NULL AND proxima_tentativa <= ?
                        ORDER BY criado_em, id
                        LIMIT ?
                        FOR UPDATE SKIP LOCKED""")
                .params(agora, LOTE)
                .query((rs, n) -> new Pendente(rs.getObject("id", UUID.class), rs.getObject("agregado_id", UUID.class),
                        rs.getString("payload"), rs.getInt("tentativas")))
                .list();

        for (Pendente evento : lote) {
            try {
                // A chave é o agregado (a conta de origem): os eventos de uma conta caem na mesma partição, em ordem.
                kafka.send(Eventos.TOPICO, evento.agregado().toString(), evento.payload()).get(10, TimeUnit.SECONDS);
                jdbc.sql("UPDATE evento_saida SET publicado_em = ? WHERE id = ?")
                        .params(Timestamp.from(clock.instant()), evento.id())
                        .update();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException | TimeoutException | RuntimeException e) {
                registrarFalha(evento, e);
                // Para o lote inteiro: publicar o próximo passaria à frente deste (e o broker provavelmente caiu).
                return;
            }
        }
    }

    private void registrarFalha(Pendente evento, Exception erro) {
        int tentativas = evento.tentativas() + 1;
        Duration espera = Duration.ofSeconds(1L << Math.min(tentativas - 1, 16));
        if (espera.compareTo(ESPERA_MAXIMA) > 0) {
            espera = ESPERA_MAXIMA;
        }
        Instant proxima = clock.instant().plus(espera);
        String mensagem = String.valueOf(erro.getMessage());
        log.warn("falha ao publicar o evento {} (tentativa {}): {}", evento.id(), tentativas, mensagem);
        jdbc.sql("UPDATE evento_saida SET tentativas = ?, proxima_tentativa = ?, ultimo_erro = ? WHERE id = ?")
                .params(tentativas, Timestamp.from(proxima), mensagem.substring(0, Math.min(mensagem.length(), 500)),
                        evento.id())
                .update();
    }
}
