package dev.trilhajava.m12.aulas.outbox;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * O outro lado do outbox: o consumidor anota cada evento processado, na MESMA transação do
 * efeito. Se o evento chegar de novo (e vai chegar: a entrega é pelo menos uma vez), é ignorado.
 */
public class Inbox {

    private final JdbcClient jdbc;
    private final Clock clock;

    public Inbox(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** true na primeira vez que o consumidor vê o evento; false nas repetições. */
    public boolean primeiraVez(UUID eventoId, String consumidor) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Inbox.primeiraVez exige a transação do efeito");
        }
        // ON CONFLICT DO NOTHING em vez de capturar a violação de chave: no PostgreSQL, um erro
        // dentro da transação a deixa abortada, e o efeito logo abaixo não poderia ser gravado.
        int inseridos = jdbc.sql("""
                INSERT INTO evento_recebido (evento_id, consumidor, recebido_em)
                VALUES (?, ?, ?)
                ON CONFLICT DO NOTHING""")
            .params(eventoId, consumidor, OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC))
            .update();
        return inseridos == 1;
    }
}
