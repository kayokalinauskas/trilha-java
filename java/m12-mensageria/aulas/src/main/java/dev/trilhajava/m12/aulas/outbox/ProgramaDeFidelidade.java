package dev.trilhajava.m12.aulas.outbox;

import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/** Um consumidor de PedidoConfirmado: 1 ponto por real inteiro gasto. Idempotente pela inbox. */
public class ProgramaDeFidelidade {

    private static final String CONSUMIDOR = "fidelidade";

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;
    private final Inbox inbox;
    private final JsonMapper json;

    public ProgramaDeFidelidade(JdbcClient jdbc, TransactionTemplate transacao, Inbox inbox, JsonMapper json) {
        this.jdbc = jdbc;
        this.transacao = transacao;
        this.inbox = inbox;
        this.json = json;
    }

    /** Devolve true se creditou pontos; false se o evento era repetido. */
    public boolean aoReceber(UUID eventoId, String payload) {
        return transacao.execute(status -> {
            if (!inbox.primeiraVez(eventoId, CONSUMIDOR)) {
                return false;                                   // já processado: não faz nada
            }
            var evento = json.readValue(payload, PedidoConfirmado.class);
            int pontos = evento.total().setScale(0, RoundingMode.DOWN).intValueExact();
            int atualizados = jdbc.sql("UPDATE saldo_de_pontos SET pontos = pontos + ? WHERE cliente = ?")
                .params(pontos, evento.cliente())
                .update();
            if (atualizados == 0) {
                jdbc.sql("INSERT INTO saldo_de_pontos (cliente, pontos) VALUES (?, ?)")
                    .params(evento.cliente(), pontos)
                    .update();
            }
            return true;
        });
    }
}
