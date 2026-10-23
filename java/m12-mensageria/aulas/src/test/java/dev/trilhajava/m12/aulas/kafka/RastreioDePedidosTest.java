package dev.trilhajava.m12.aulas.kafka;

import static dev.trilhajava.m12.aulas.kafka.StatusDoPedido.CRIADO;
import static dev.trilhajava.m12.aulas.kafka.StatusDoPedido.ENVIADO;
import static dev.trilhajava.m12.aulas.kafka.StatusDoPedido.PAGO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class RastreioDePedidosTest {

    final RastreioDePedidos rastreio = new RastreioDePedidos();

    static EventoDePedido evento(String numero, StatusDoPedido status) {
        return new EventoDePedido(numero, status, Instant.parse("2026-10-05T13:00:00Z"));
    }

    @Test
    void reentregaDeUmaEtapaJaVistaEhIgnorada() {
        rastreio.aplicar(evento("PED-1001", CRIADO));
        rastreio.aplicar(evento("PED-1001", PAGO));

        var resultado = rastreio.aplicar(evento("PED-1001", PAGO));

        assertThat(resultado).isEqualTo(RastreioDePedidos.Resultado.REPETIDO);
        assertThat(rastreio.historico("PED-1001")).containsExactly(CRIADO, PAGO);
    }

    @Test
    void eventoQuePulaEtapasChegouForaDeOrdem() {
        rastreio.aplicar(evento("PED-1001", CRIADO));

        assertThatThrownBy(() -> rastreio.aplicar(evento("PED-1001", ENVIADO)))
                .isInstanceOf(TransicaoInvalidaException.class)
                .hasMessage("pedido PED-1001: recebido ENVIADO, mas o status atual é CRIADO");
        assertThat(rastreio.status("PED-1001")).contains(CRIADO);
    }
}
