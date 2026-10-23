package dev.trilhajava.m12.aulas.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.trilhajava.m12.aulas.outbox.Apoio.BrokerEmMemoria;
import dev.trilhajava.m12.aulas.outbox.Apoio.Relogio;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.json.JsonMapper;

class EscritaDuplaTest {

    final Apoio.Banco banco = Apoio.bancoNovo();
    final BrokerEmMemoria broker = new BrokerEmMemoria();
    final Relogio relogio = new Relogio();
    final JsonMapper json = JsonMapper.builder().build();

    // 12 pacotes, mas só há 10 no estoque: a CHECK (estoque >= 0) derruba a transação.
    final NovoPedido semEstoque = new NovoPedido("ana", "CAFE-500G", 12, new BigDecimal("358.80"));

    @Test
    void escritaDuplaPublicaUmEventoFantasma_outboxNao() {
        var ingenuo = new ServicoDePedidosIngenuo(banco.jdbc(), banco.transacao(), broker, json, relogio);
        assertThatThrownBy(() -> ingenuo.confirmar(semEstoque))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(contar("pedido")).isZero();          // o pedido não existe...
        assertThat(broker.recebidas).hasSize(1);        // ...mas o mundo foi avisado dele

        var comOutbox = new ServicoDePedidos(banco.jdbc(), banco.transacao(),
                new Outbox(banco.jdbc(), relogio), json, relogio);
        assertThatThrownBy(() -> comOutbox.confirmar(semEstoque))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(contar("pedido")).isZero();
        assertThat(contar("evento_saida")).isZero();    // o rollback levou o evento junto
    }

    @Test
    void outboxForaDeTransacaoEhErroDeUso() {
        var outbox = new Outbox(banco.jdbc(), relogio);
        assertThatThrownBy(() -> outbox.registrar("pedido", "P-1", "PedidoConfirmado", "{}"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("exige uma transação");
    }

    private long contar(String tabela) {
        return banco.jdbc().sql("SELECT COUNT(*) FROM " + tabela).query(Long.class).single();
    }
}
