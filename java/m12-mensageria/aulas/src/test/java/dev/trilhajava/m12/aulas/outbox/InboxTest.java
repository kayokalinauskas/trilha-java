package dev.trilhajava.m12.aulas.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import dev.trilhajava.m12.aulas.outbox.Apoio.Relogio;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class InboxTest {

    @Test
    void eventoRepetidoNaoCreditaPontosDuasVezes() {
        var banco = Apoio.bancoNovo();
        var json = JsonMapper.builder().build();
        var fidelidade = new ProgramaDeFidelidade(banco.jdbc(), banco.transacao(),
                new Inbox(banco.jdbc(), new Relogio()), json);

        var eventoId = UUID.randomUUID();
        var payload = json.writeValueAsString(new PedidoConfirmado(
                UUID.randomUUID(), "ana", "CAFE-500G", 2, new BigDecimal("59.80")));

        assertThat(fidelidade.aoReceber(eventoId, payload)).isTrue();
        assertThat(fidelidade.aoReceber(eventoId, payload)).isFalse();   // o relay reenviou

        int pontos = banco.jdbc().sql("SELECT pontos FROM saldo_de_pontos WHERE cliente = 'ana'")
            .query(Integer.class).single();
        assertThat(pontos).isEqualTo(59);
    }
}
