package dev.trilhajava.m12.aulas.idempotencia;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Credita o cashback na conta uma única vez, por mais que o broker entregue o mesmo evento.
 * Quem chama (um @KafkaListener, um @RabbitListener) só confirma a mensagem depois que este
 * método retorna sem exceção.
 */
public class ConsumidorDeCashback {

    static final String CONSUMIDOR = "cashback";

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;
    private final Notificador notificador;
    private final Clock relogio;

    public ConsumidorDeCashback(JdbcClient jdbc, TransactionTemplate transacao,
                                Notificador notificador, Clock relogio) {
        this.jdbc = jdbc;
        this.transacao = transacao;
        this.notificador = notificador;
        this.relogio = relogio;
    }

    public Resultado processar(CashbackConcedido evento) {
        return transacao.execute(status -> processarNaTransacao(evento));
    }

    private Resultado processarNaTransacao(CashbackConcedido evento) {
        // 1. A marca na inbox: a chave primária decide quem chegou primeiro.
        int marcada = jdbc.sql("""
                INSERT INTO mensagem_processada (consumidor, mensagem_id, processada_em)
                VALUES (?, ?, ?)
                ON CONFLICT DO NOTHING""")
            .params(CONSUMIDOR, evento.eventoId(), LocalDateTime.now(relogio))
            .update();
        if (marcada == 0) {
            return Resultado.DUPLICADO;                 // esta mensagem já foi aplicada
        }

        // 2. O efeito, protegido também pela chave de negócio (uma compra, um cashback).
        //    Se a conta não existe, a chave estrangeira lança, e o rollback desfaz a marca também.
        int lancado = jdbc.sql("""
                INSERT INTO lancamento (conta_id, compra_id, valor)
                VALUES (?, ?, ?)
                ON CONFLICT DO NOTHING""")
            .params(evento.contaId(), evento.compraId(), evento.valor())
            .update();
        if (lancado == 0) {
            return Resultado.DUPLICADO;                 // mesma compra, outro eventoId
        }

        jdbc.sql("UPDATE conta SET saldo = saldo + ? WHERE id = ?")
            .params(evento.valor(), evento.contaId())
            .update();                                  // incremento: seguro porque está protegido acima

        // 3. O efeito externo, só depois do commit.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificador.cashbackCreditado(evento.contaId(), evento.valor());
            }
        });
        return Resultado.PROCESSADO;
    }
}
