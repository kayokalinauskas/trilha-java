package dev.trilhajava.m12.aulas.outbox;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * A versão com ESCRITA DUPLA: grava no banco e publica no broker na mesma requisição.
 * Existe só para a aula mostrar o defeito. Não copie.
 */
public class ServicoDePedidosIngenuo {

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;
    private final Publicador broker;
    private final JsonMapper json;
    private final Clock clock;

    public ServicoDePedidosIngenuo(JdbcClient jdbc, TransactionTemplate transacao, Publicador broker,
            JsonMapper json, Clock clock) {
        this.jdbc = jdbc;
        this.transacao = transacao;
        this.broker = broker;
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
            broker.publicar(new Mensagem(UUID.randomUUID(), id.toString(), "PedidoConfirmado",
                    json.writeValueAsString(evento)));                         // 1. o evento SAIU

            jdbc.sql("UPDATE produto SET estoque = estoque - ? WHERE sku = ?")   // 2. a CHECK pode falhar
                .params(novo.quantidade(), novo.sku())
                .update();
            return id;                                                         // 3. só aqui o commit
        });
    }
}
