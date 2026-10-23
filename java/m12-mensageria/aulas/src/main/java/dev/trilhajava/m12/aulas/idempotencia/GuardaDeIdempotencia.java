package dev.trilhajava.m12.aulas.idempotencia;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.function.Supplier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Idempotency-Key numa API HTTP: a primeira requisição com a chave executa a operação e guarda a
 * resposta; as repetições recebem a MESMA resposta, sem executar de novo. Tudo numa transação só:
 * a reserva da chave, o efeito da operação (no mesmo banco) e a resposta guardada.
 */
public class GuardaDeIdempotencia {

    private record RespostaGuardada(String hash, int status, String corpo) {
    }

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;
    private final Clock relogio;

    public GuardaDeIdempotencia(JdbcClient jdbc, TransactionTemplate transacao, Clock relogio) {
        this.jdbc = jdbc;
        this.transacao = transacao;
        this.relogio = relogio;
    }

    public RespostaHttp executar(String clienteId, String chave, String corpoDaRequisicao,
                                 Supplier<RespostaHttp> operacao) {
        if (chave == null || chave.isBlank()) {
            return new RespostaHttp(400, "{\"erro\":\"Idempotency-Key obrigatório\"}");
        }
        String hash = sha256(corpoDaRequisicao);
        return transacao.execute(status -> {
            int reservada = jdbc.sql("""
                    INSERT INTO chave_de_idempotencia (cliente_id, chave, hash_da_requisicao, criada_em)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT DO NOTHING""")
                .params(clienteId, chave, hash, LocalDateTime.now(relogio))
                .update();
            if (reservada == 0) {
                return repeticao(clienteId, chave, hash);
            }
            RespostaHttp resposta = operacao.get();     // grava no mesmo banco, na mesma transação
            jdbc.sql("""
                    UPDATE chave_de_idempotencia SET status_http = ?, corpo_da_resposta = ?
                    WHERE cliente_id = ? AND chave = ?""")
                .params(resposta.status(), resposta.corpo(), clienteId, chave)
                .update();
            return resposta;
        });
    }

    private RespostaHttp repeticao(String clienteId, String chave, String hash) {
        RespostaGuardada guardada = jdbc.sql("""
                SELECT hash_da_requisicao, status_http, corpo_da_resposta
                FROM chave_de_idempotencia WHERE cliente_id = ? AND chave = ?""")
            .params(clienteId, chave)
            .query((rs, linha) -> new RespostaGuardada(rs.getString(1), rs.getInt(2), rs.getString(3)))
            .single();
        if (!guardada.hash().equals(hash)) {
            return new RespostaHttp(422, "{\"erro\":\"Idempotency-Key reutilizada com outro corpo\"}");
        }
        return new RespostaHttp(guardada.status(), guardada.corpo());
    }

    private static String sha256(String texto) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("toda JVM traz SHA-256", e);
        }
    }
}
