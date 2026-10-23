package dev.trilhajava.m12.aulas.esquemas;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class UpcasterTest {

    @Test
    void eventoV1ChegaAoConsumidorComoV2() {
        PedidoConfirmado pedido = Exemplos.leitor().ler(Exemplos.ler("v1.json"));

        assertThat(pedido).isEqualTo(new PedidoConfirmado(
                "PED-100",
                "CLI-42",
                new ValorTotal(new BigDecimal("112.30"), "BRL"),
                "01310-100",
                FormaDePagamento.DESCONHECIDA));
    }
}
