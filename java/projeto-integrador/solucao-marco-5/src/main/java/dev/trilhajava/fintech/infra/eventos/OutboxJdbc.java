package dev.trilhajava.fintech.infra.eventos;

import dev.trilhajava.fintech.aplicacao.PublicadorDeEventos;
import dev.trilhajava.fintech.dominio.EventoDeDominio;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * "Publicar" um evento = gravar uma linha na tabela evento_saida, na transação do caso de uso.
 * O JdbcClient usa a mesma conexão da transação JPA: ou tudo é confirmado, ou nada.
 */
@Component
class OutboxJdbc implements PublicadorDeEventos {

    private final JdbcClient jdbc;
    private final JsonMapper json;

    OutboxJdbc(JdbcClient jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** MANDATORY: sem transação em andamento, é bug — o evento sairia sem o negócio (ou vice-versa). */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publicar(EventoDeDominio evento) {
        UUID id = UUID.randomUUID();
        Timestamp agora = Timestamp.from(evento.ocorridoEm());
        jdbc.sql("""
                        INSERT INTO evento_saida (id, agregado_id, tipo, payload, criado_em, proxima_tentativa)
                        VALUES (?, ?, ?, ?, ?, ?)""")
                .params(id, evento.agregado(), evento.tipo(), json.writeValueAsString(EventoJson.de(id, evento)),
                        agora, agora)
                .update();
    }
}
