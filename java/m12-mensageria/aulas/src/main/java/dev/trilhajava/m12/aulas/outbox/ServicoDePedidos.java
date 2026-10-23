package dev.trilhajava.m12.aulas.outbox;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/** O mesmo caso de uso, com a outbox: o evento é uma linha a mais na MESMA transação. */
public class ServicoDePedidos {

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;
    private final Outbox outbox;
    private final JsonMapper json;
    private final Clock clock;

    public ServicoDePedidos(JdbcClient jdbc, TransactionTemplate transacao, Outbox outbox,
            JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.transacao = transacao;
        this.outbox = outbox;
        this.json = json;
        this.clock = clock;
    }

    public UUID confirmar(NovoPedido novo) {
        return transacao.execute(status -> {
            var id = UUID.randomUUID();
            jdbc.sql("INSERT INTO pedido (id, cliente, sku, quantidade, total, criado_em) VALUES (?, ?, ?, ?, ?, ?)")
                .params(id, novo.cliente(), novo.sku(), novo.quantidade(), novo.total(),
                        OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC))
                .update();

            var evento = new PedidoConfirmado(id, novo.cliente(), novo.sku(), novo.quantidade(), novo.total());
            outbox.registrar("pedido", id.toString(), "PedidoConfirmado",
                    json.writeValueAsString(evento));                          // 1. só um INSERT

            jdbc.sql("UPDATE produto SET estoque = estoque - ? WHERE sku = ?")   // 2. se falhar, rollback
                .params(novo.quantidade(), novo.sku())                         //    leva o evento junto
                .update();
            return id;
        });
    }
}
