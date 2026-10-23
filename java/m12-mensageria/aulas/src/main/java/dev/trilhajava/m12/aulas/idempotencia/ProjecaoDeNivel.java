package dev.trilhajava.m12.aulas.idempotencia;

import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Idempotência natural: a projeção GRAVA o estado que veio no evento (set), em vez de somar
 * (incremento). A versão descarta o que chegou atrasado. Não precisa de tabela de deduplicação.
 */
public class ProjecaoDeNivel {

    private final JdbcClient jdbc;

    public ProjecaoDeNivel(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Devolve true se o evento mudou a projeção; false se era repetido ou obsoleto. */
    public boolean aplicar(NivelDoClienteAlterado evento) {
        int linhas = jdbc.sql("""
                MERGE INTO nivel_do_cliente AS atual
                USING (VALUES (?, ?, CAST(? AS BIGINT))) AS novo (cliente_id, nivel, versao)
                   ON atual.cliente_id = novo.cliente_id
                WHEN MATCHED AND atual.versao < novo.versao THEN
                    UPDATE SET nivel = novo.nivel, versao = novo.versao
                WHEN NOT MATCHED THEN
                    INSERT (cliente_id, nivel, versao) VALUES (novo.cliente_id, novo.nivel, novo.versao)""")
            .params(evento.clienteId(), evento.nivel(), evento.versao())
            .update();
        return linhas > 0;
    }

    public Optional<String> nivelDe(String clienteId) {
        return jdbc.sql("SELECT nivel FROM nivel_do_cliente WHERE cliente_id = ?")
            .param(clienteId)
            .query(String.class)
            .optional();
    }
}
