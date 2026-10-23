package dev.trilhajava.m12.pagamentos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Processa eventos do gateway de forma idempotente: a mesma mensagem, entregue duas vezes (ou
 * em paralelo), produz o efeito uma única vez.
 *
 * Tudo acontece numa transação: a marca "mensagem processada", a mudança do pedido, o pagamento
 * e o lançamento contábil são gravados juntos ou nenhum é. O aviso ao cliente, que não dá para
 * desfazer, só sai depois do commit.
 */
@Service
public class ConsumidorDePagamentos {

    static final String CONSUMIDOR = "pagamentos";

    private final JdbcClient jdbc;
    private final Contabilidade contabilidade;
    private final NotificadorDePagamento notificador;
    private final Clock relogio;

    public ConsumidorDePagamentos(JdbcClient jdbc, Contabilidade contabilidade, NotificadorDePagamento notificador,
            Clock relogio) {
        this.jdbc = jdbc;
        this.contabilidade = contabilidade;
        this.notificador = notificador;
        this.relogio = relogio;
    }

    @Transactional
    public ResultadoDoProcessamento processar(EventoDePagamento evento) {
        Timestamp agora = Timestamp.from(Instant.now(relogio));

        // 1. Deduplicação. Se outra transação inseriu a mesma chave e ainda não terminou, o
        //    PostgreSQL espera por ela: se ela confirmar, este INSERT não insere nada (0 linhas);
        //    se ela desfizer, este insere. Não há janela entre "verificar" e "gravar".
        int inseridas = jdbc.sql("""
                        INSERT INTO mensagem_processada (consumidor, mensagem_id, processada_em)
                        VALUES (?, ?, ?)
                        ON CONFLICT DO NOTHING""")
                .params(CONSUMIDOR, evento.mensagemId(), agora)
                .update();
        if (inseridas == 0) {
            return ResultadoDoProcessamento.DUPLICADO;
        }

        // 2. Trava o pedido: eventos diferentes do mesmo pedido são aplicados um de cada vez.
        Pedido pedido = jdbc.sql("SELECT valor, versao_evento FROM pedido WHERE id = ? FOR UPDATE")
                .param(evento.pedidoId())
                .query((rs, linha) -> new Pedido(rs.getBigDecimal("valor"), rs.getLong("versao_evento")))
                .optional()
                .orElseThrow(() -> new PedidoDesconhecidoException(evento.pedidoId()));

        // 3. Fora de ordem: um evento mais antigo que o estado atual não desfaz o mais novo.
        if (evento.sequencia() <= pedido.versaoEvento()) {
            return ResultadoDoProcessamento.OBSOLETO;
        }
        if (evento.valor().compareTo(pedido.valor()) != 0) {
            throw new ValorDivergenteException(evento.pedidoId(), pedido.valor(), evento.valor());
        }

        // 4. O efeito.
        String status = evento.tipo() == EventoDePagamento.Tipo.APROVADO ? "PAGO" : "ESTORNADO";
        jdbc.sql("UPDATE pedido SET status = ?, versao_evento = ?, atualizado_em = ? WHERE id = ?")
                .params(status, evento.sequencia(), agora, evento.pedidoId())
                .update();
        jdbc.sql("INSERT INTO pagamento (pedido_id, tipo, valor, transacao) VALUES (?, ?, ?, ?)")
                .params(evento.pedidoId(), evento.tipo().name(), evento.valor(), evento.transacao())
                .update();
        BigDecimal lancamento = evento.tipo() == EventoDePagamento.Tipo.APROVADO ? evento.valor() : evento.valor().negate();
        contabilidade.lancar(evento.pedidoId(), lancamento, evento.tipo() + " " + evento.transacao());

        // 5. O efeito externo, só se (e quando) tudo acima for confirmado.
        depoisDoCommit(() -> {
            if (evento.tipo() == EventoDePagamento.Tipo.APROVADO) {
                notificador.pagamentoAprovado(evento.pedidoId());
            } else {
                notificador.pagamentoEstornado(evento.pedidoId());
            }
        });
        return ResultadoDoProcessamento.PROCESSADO;
    }

    private static void depoisDoCommit(Runnable acao) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                acao.run();
            }
        });
    }

    private record Pedido(BigDecimal valor, long versaoEvento) {
    }
}
