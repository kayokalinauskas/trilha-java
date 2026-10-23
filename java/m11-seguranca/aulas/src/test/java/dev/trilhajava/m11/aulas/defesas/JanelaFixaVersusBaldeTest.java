package dev.trilhajava.m11.aulas.defesas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** O defeito clássico da janela fixa: na virada, passa o dobro do limite. */
class JanelaFixaVersusBaldeTest {

    static final Instant MEIO_DIA = Instant.parse("2026-10-05T12:00:00Z");

    @Test
    void janelaFixaDeixaPassarODobroNaVirada() {
        var relogio = new RelogioAjustavel(MEIO_DIA.plusMillis(59_500));
        var janela = new JanelaFixa(10, Duration.ofMinutes(1), relogio);

        int antes = rajadaDe10(janela);
        relogio.avancar(Duration.ofSeconds(1));               // 12:01:00.5: janela nova, contador zerado
        int depois = rajadaDe10(janela);

        assertThat(antes + depois).isEqualTo(20);             // 20 requisições em 1 segundo
    }

    @Test
    void tokenBucketSegueALimiteNaMesmaRajada() {
        var relogio = new RelogioAjustavel(MEIO_DIA.plusMillis(59_500));
        var balde = new LimitadorDeCreditos(new PoliticaDeLimite("por-cliente", 10, Duration.ofMinutes(1)), relogio);

        int antes = rajadaDe10(balde);
        relogio.avancar(Duration.ofSeconds(1));               // 1 s rende 1/6 de crédito: nada
        int depois = rajadaDe10(balde);

        assertThat(antes).isEqualTo(10);
        assertThat(depois).isZero();
    }

    private static int rajadaDe10(JanelaFixa janela) {
        int aceitas = 0;
        for (int i = 0; i < 10; i++) {
            aceitas += janela.tentar() ? 1 : 0;
        }
        return aceitas;
    }

    private static int rajadaDe10(LimitadorDeCreditos balde) {
        int aceitas = 0;
        for (int i = 0; i < 10; i++) {
            aceitas += balde.consumir("cliente-1", 1).permitido() ? 1 : 0;
        }
        return aceitas;
    }
}
