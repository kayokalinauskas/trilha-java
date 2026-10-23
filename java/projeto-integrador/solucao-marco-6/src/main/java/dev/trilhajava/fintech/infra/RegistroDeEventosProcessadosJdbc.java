package dev.trilhajava.fintech.infra;

import dev.trilhajava.fintech.aplicacao.RegistroDeEventosProcessados;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
class RegistroDeEventosProcessadosJdbc implements RegistroDeEventosProcessados {

    private final JdbcClient jdbc;
    private final Clock clock;

    RegistroDeEventosProcessadosJdbc(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * ON CONFLICT DO NOTHING: uma entrega repetida (mesmo em paralelo) não insere nada e não lança
     * exceção — que, no PostgreSQL, abortaria a transação inteira.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean registrar(String consumidor, UUID evento) {
        return jdbc.sql("""
                        INSERT INTO evento_processado (consumidor, evento_id, processado_em)
                        VALUES (?, ?, ?)
                        ON CONFLICT DO NOTHING""")
                .params(consumidor, evento, Timestamp.from(clock.instant()))
                .update() == 1;
    }
}
