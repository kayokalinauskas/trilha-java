package dev.trilhajava.m10.aulas.reembolso;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Os dados que todos os testes do reembolso compartilham. */
final class DadosDeTeste {

    static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    /** O "agora" dos testes: 10/09/2026, meio-dia em São Paulo. */
    static final Instant AGORA = Instant.parse("2026-09-10T15:00:00Z");

    /** Um relógio parado em AGORA: o tempo não anda enquanto o teste roda. */
    static final Clock RELOGIO = Clock.fixed(AGORA, SAO_PAULO);

    /** Entregue 3 dias antes de AGORA: dentro do prazo de 7 dias. */
    static final LocalDate ENTREGA_RECENTE = LocalDate.of(2026, 9, 7);

    private DadosDeTeste() {
    }

    /** PED-100: 2 × café a 49,90 + 1 × filtro a 12,50 = 112,30. */
    static Pedido pedidoEntregueEm(LocalDate entrega) {
        return new Pedido("PED-100", "cliente-42", "AUT-777",
                List.of(new Item("CAFE-1KG", 2, new BigDecimal("49.90")),
                        new Item("FILTRO-102", 1, new BigDecimal("12.50"))),
                entrega);
    }

    static Pedido pedidoDentroDoPrazo() {
        return pedidoEntregueEm(ENTREGA_RECENTE);
    }
}
