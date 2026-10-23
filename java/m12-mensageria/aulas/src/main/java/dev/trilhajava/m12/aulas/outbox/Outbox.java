package dev.trilhajava.m12.aulas.outbox;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Grava eventos na tabela evento_saida. "Publicar", para o caso de uso, vira um INSERT
 * na transação que já está aberta: commit grava negócio e evento; rollback, nenhum dos dois.
 */
public class Outbox {

    private final JdbcClient jdbc;
    private final Clock clock;

    public Outbox(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    public UUID registrar(String agregadoTipo, String agregadoId, String tipo, String payload) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            // Mesmo papel do @Transactional(propagation = MANDATORY): fora de uma transação,
            // o evento seria gravado sozinho e se soltaria do negócio.
            throw new IllegalStateException("Outbox.registrar exige uma transação em andamento");
        }
        var id = UUID.randomUUID();
        var agora = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        jdbc.sql("""
                INSERT INTO evento_saida
                    (id, agregado_tipo, agregado_id, tipo, payload, criado_em, proxima_tentativa)
                VALUES (?, ?, ?, ?, ?, ?, ?)""")
            .params(id, agregadoTipo, agregadoId, tipo, payload, agora, agora)
            .update();
        return id;
    }
}
