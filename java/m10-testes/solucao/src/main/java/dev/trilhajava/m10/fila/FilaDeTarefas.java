package dev.trilhajava.m10.fila;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Uma fila de tarefas em cima do PostgreSQL — o padrão por trás de bibliotecas como db-scheduler,
 * JobRunr e o Solid Queue do Rails. Vários trabalhadores (threads ou instâncias) consomem a mesma
 * tabela sem nunca pegar a mesma tarefa, graças ao FOR UPDATE SKIP LOCKED.
 */
@Repository
public class FilaDeTarefas {

    static final int MAXIMO_DE_TENTATIVAS = 3;

    private final JdbcClient jdbc;

    public FilaDeTarefas(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long enfileirar(String tipo, String payloadJson) {
        return jdbc.sql("INSERT INTO tarefa (tipo, payload) VALUES (:tipo, CAST(:payload AS jsonb)) RETURNING id")
                .param("tipo", tipo)
                .param("payload", payloadJson)
                .query(Long.class)
                .single();
    }

    /**
     * Um único comando atômico: escolhe a pendente mais antiga que NINGUÉM está travando
     * (SKIP LOCKED pula as linhas travadas por outros trabalhadores em vez de esperar por elas)
     * e a marca como reservada.
     */
    public Optional<Tarefa> reservarProxima(String trabalhador) {
        return jdbc.sql("""
                UPDATE tarefa
                   SET status = 'EM_PROCESSAMENTO', reservada_por = :trabalhador,
                       reservada_em = now(), tentativas = tentativas + 1
                 WHERE id = (SELECT id FROM tarefa
                              WHERE status = 'PENDENTE'
                              ORDER BY criada_em, id
                              LIMIT 1
                              FOR UPDATE SKIP LOCKED)
                RETURNING id, tipo, payload::text AS payload, status, tentativas, reservada_por""")
                .param("trabalhador", trabalhador)
                .query((rs, n) -> new Tarefa(rs.getLong("id"), rs.getString("tipo"), rs.getString("payload"),
                        StatusTarefa.valueOf(rs.getString("status")), rs.getInt("tentativas"),
                        rs.getString("reservada_por")))
                .optional();
    }

    public void concluir(long id) {
        int linhas = jdbc.sql("UPDATE tarefa SET status = 'CONCLUIDA' WHERE id = :id AND status = 'EM_PROCESSAMENTO'")
                .param("id", id)
                .update();
        exigirReservada(id, linhas);
    }

    /** Volta para a fila, a menos que tenha esgotado as tentativas. */
    public void falhar(long id, String erro) {
        int linhas = jdbc.sql("""
                UPDATE tarefa
                   SET status = CASE WHEN tentativas >= :maximo THEN 'FALHOU' ELSE 'PENDENTE' END,
                       ultimo_erro = :erro, reservada_por = NULL, reservada_em = NULL
                 WHERE id = :id AND status = 'EM_PROCESSAMENTO'""")
                .param("maximo", MAXIMO_DE_TENTATIVAS)
                .param("erro", erro)
                .param("id", id)
                .update();
        exigirReservada(id, linhas);
    }

    public Map<StatusTarefa, Long> contagemPorStatus() {
        Map<StatusTarefa, Long> contagem = new EnumMap<>(StatusTarefa.class);
        for (StatusTarefa status : StatusTarefa.values()) {
            contagem.put(status, 0L);
        }
        jdbc.sql("SELECT status, count(*) AS total FROM tarefa GROUP BY status")
                .query((rs, n) -> contagem.put(StatusTarefa.valueOf(rs.getString("status")), rs.getLong("total")))
                .list();
        return contagem;
    }

    private static void exigirReservada(long id, int linhasAfetadas) {
        if (linhasAfetadas == 0) {
            throw new IllegalStateException("A tarefa " + id + " não está em processamento");
        }
    }
}
