package dev.trilhajava.m10.aulas.junit;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** O "agora" e os atalhos de data que todos os testes da agenda compartilham. */
final class DadosDaAgenda {

    static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    /** Segunda-feira, 14/09/2026, 9h da manhã em São Paulo. */
    static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 14, 9, 0);
    static final LocalDate HOJE = AGORA.toLocalDate();
    static final LocalDate AMANHA = HOJE.plusDays(1);

    /** Um relógio parado em AGORA: o tempo não anda enquanto o teste roda. */
    static final Clock RELOGIO = relogioEm(AGORA);

    private DadosDaAgenda() {
    }

    static Clock relogioEm(LocalDateTime instante) {
        return Clock.fixed(instante.atZone(SAO_PAULO).toInstant(), SAO_PAULO);
    }

    static LocalDateTime hojeAs(int hora, int minuto) {
        return HOJE.atTime(hora, minuto);
    }

    static LocalDateTime amanhaAs(int hora, int minuto) {
        return AMANHA.atTime(hora, minuto);
    }
}
