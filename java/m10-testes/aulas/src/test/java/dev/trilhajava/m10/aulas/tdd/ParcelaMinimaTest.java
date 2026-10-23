package dev.trilhajava.m10.aulas.tdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

/** Solução do checkpoint da aula 10.06: a regra da parcela mínima, escrita com TDD. */
class ParcelaMinimaTest {

    SimuladorDeParcelamento simulador = new SimuladorDeParcelamento(
            Clock.fixed(Instant.parse("2026-03-10T15:00:00Z"), ZoneId.of("America/Sao_Paulo")));

    @Test
    void aceitaParcelasDeExatamenteCincoReais() {
        assertThat(simulador.simular(new BigDecimal("15.00"), 3))
                .extracting(Parcela::valor)
                .containsExactly(new BigDecimal("5.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));
    }

    @Test
    void recusaQuandoAsParcelasFicariamAbaixoDeCincoReais() {
        assertThatThrownBy(() -> simulador.simular(new BigDecimal("14.99"), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cada parcela deve ter no mínimo R$ 5,00");
    }

    @Test
    void aVistaNaoTemValorMinimo() {
        assertThat(simulador.simular(new BigDecimal("3.00"), 1))
                .extracting(Parcela::valor)
                .containsExactly(new BigDecimal("3.00"));
    }
}
