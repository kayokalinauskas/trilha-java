package dev.trilhajava.m12.aulas.idempotencia;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Apaga as marcas mais antigas que a janela de deduplicação. A janela precisa ser MAIOR que o
 * tempo máximo em que uma duplicata ainda pode chegar (retenção do tópico, DLQ, replay).
 */
public class LimpezaDaDeduplicacao {

    private final JdbcClient jdbc;
    private final Clock relogio;

    public LimpezaDaDeduplicacao(JdbcClient jdbc, Clock relogio) {
        this.jdbc = jdbc;
        this.relogio = relogio;
    }

    public int apagarMarcasAntigas(Duration janela) {
        LocalDateTime limite = LocalDateTime.now(relogio).minus(janela);
        return jdbc.sql("DELETE FROM mensagem_processada WHERE processada_em < ?")
            .param(limite)
            .update();
    }
}
