package dev.trilhajava.m12.aulas.outbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * O relay por polling: lê um lote de pendentes, publica na ordem, marca os publicados e
 * agenda nova tentativa para os que falharam. Garantia: PELO MENOS UMA VEZ.
 */
public class RelayDoOutbox {

    public record Resultado(int publicados, int falhas, int adiados) {}

    private record EventoDeSaida(UUID id, String agregadoId, String tipo, String payload,
            int tentativas, Instant proximaTentativa) {}

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;
    private final Publicador publicador;
    private final Clock clock;
    private final int tamanhoDoLote;
    private final Duration atrasoInicial;
    private final Duration atrasoMaximo;

    public RelayDoOutbox(JdbcClient jdbc, TransactionTemplate transacao, Publicador publicador, Clock clock,
            int tamanhoDoLote, Duration atrasoInicial, Duration atrasoMaximo) {
        this.jdbc = jdbc;
        this.transacao = transacao;
        this.publicador = publicador;
        this.clock = clock;
        this.tamanhoDoLote = tamanhoDoLote;
        this.atrasoInicial = atrasoInicial;
        this.atrasoMaximo = atrasoMaximo;
    }

    public Resultado executar() {
        return transacao.execute(status -> {
            var agora = clock.instant();
            var lote = jdbc.sql("""
                    SELECT id, agregado_id, tipo, payload, tentativas, proxima_tentativa
                    FROM evento_saida
                    WHERE publicado_em IS NULL
                    ORDER BY posicao
                    LIMIT ?
                    FOR UPDATE SKIP LOCKED""")
                .param(tamanhoDoLote)
                .query(RelayDoOutbox::mapear)
                .list();

            var travados = new HashSet<String>();   // agregados que não podem andar nesta rodada
            int publicados = 0, falhas = 0, adiados = 0;
            for (var evento : lote) {
                if (travados.contains(evento.agregadoId()) || evento.proximaTentativa().isAfter(agora)) {
                    travados.add(evento.agregadoId());   // quem vem depois do mesmo agregado espera
                    adiados++;
                    continue;
                }
                var erro = tentarPublicar(evento);
                if (erro.isEmpty()) {
                    jdbc.sql("UPDATE evento_saida SET publicado_em = ? WHERE id = ?")
                        .params(utc(agora), evento.id())
                        .update();
                    publicados++;
                } else {
                    int tentativas = evento.tentativas() + 1;
                    jdbc.sql("""
                            UPDATE evento_saida
                            SET tentativas = ?, proxima_tentativa = ?, ultimo_erro = ?
                            WHERE id = ?""")
                        .params(tentativas, utc(agora.plus(atraso(tentativas))), erro.get(), evento.id())
                        .update();
                    travados.add(evento.agregadoId());
                    falhas++;
                }
            }
            return new Resultado(publicados, falhas, adiados);
        });
    }

    /** Apaga os publicados há mais tempo que a retenção. Pendentes nunca são apagados. */
    public int limpar(Duration retencao) {
        var limite = clock.instant().minus(retencao);
        return jdbc.sql("DELETE FROM evento_saida WHERE publicado_em < ?")
            .param(utc(limite))
            .update();
    }

    private Optional<String> tentarPublicar(EventoDeSaida evento) {
        try {
            publicador.publicar(new Mensagem(evento.id(), evento.agregadoId(), evento.tipo(), evento.payload()));
            return Optional.empty();
        } catch (RuntimeException falha) {
            var mensagem = falha.getClass().getSimpleName() + ": " + falha.getMessage();
            return Optional.of(mensagem.length() > 500 ? mensagem.substring(0, 500) : mensagem);
        }
    }

    /** Backoff exponencial: 1ª falha = atrasoInicial, depois dobra, até o teto. */
    Duration atraso(int tentativas) {
        var atraso = atrasoInicial;
        for (int i = 1; i < tentativas && atraso.compareTo(atrasoMaximo) < 0; i++) {
            atraso = atraso.multipliedBy(2);
        }
        return atraso.compareTo(atrasoMaximo) > 0 ? atrasoMaximo : atraso;
    }

    private static EventoDeSaida mapear(ResultSet rs, int linha) throws SQLException {
        return new EventoDeSaida(
                rs.getObject("id", UUID.class),
                rs.getString("agregado_id"),
                rs.getString("tipo"),
                rs.getString("payload"),
                rs.getInt("tentativas"),
                rs.getObject("proxima_tentativa", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime utc(Instant instante) {
        return OffsetDateTime.ofInstant(instante, ZoneOffset.UTC);
    }
}
