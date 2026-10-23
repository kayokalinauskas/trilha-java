package dev.trilhajava.m05;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 05.06 — Agenda de salas (intervalos)")
class AgendaDeSalasTest {

    private static Intervalo i(int inicio, int fim) {
        return new Intervalo(inicio, fim);
    }

    @Test
    @DisplayName("mescla intervalos sobrepostos e encostados")
    void mesclar() {
        assertThat(AgendaDeSalas.mesclar(List.of(i(540, 600), i(570, 630), i(700, 720), i(630, 660))))
                .containsExactly(i(540, 660), i(700, 720));
        assertThat(AgendaDeSalas.mesclar(List.of())).isEmpty();
    }

    @Test
    @DisplayName("não altera a lista recebida")
    void naoAltera() {
        List<Intervalo> entrada = new ArrayList<>(List.of(i(600, 700), i(500, 550)));
        AgendaDeSalas.mesclar(entrada);
        assertThat(entrada).containsExactly(i(600, 700), i(500, 550));
    }

    @Test
    @DisplayName("uma pessoa consegue ir a todas as reuniões?")
    void todas() {
        assertThat(AgendaDeSalas.semConflitos(List.of(i(540, 600), i(600, 660)))).isTrue();
        assertThat(AgendaDeSalas.semConflitos(List.of(i(540, 601), i(600, 660)))).isFalse();
    }

    @Test
    @DisplayName("mínimo de salas para todas as reuniões")
    void salas() {
        assertThat(AgendaDeSalas.minimoDeSalas(List.of(i(540, 600), i(550, 700), i(610, 650), i(700, 720)))).isEqualTo(2);
        assertThat(AgendaDeSalas.minimoDeSalas(List.of(i(0, 10), i(0, 10), i(0, 10)))).isEqualTo(3);
        assertThat(AgendaDeSalas.minimoDeSalas(List.of())).isZero();
    }

    @Test
    @DisplayName("horários livres dentro do expediente")
    void livres() {
        assertThat(AgendaDeSalas.horariosLivres(List.of(i(600, 660), i(540, 570), i(640, 700)), 480, 1080))
                .containsExactly(i(480, 540), i(570, 600), i(700, 1080));
        assertThat(AgendaDeSalas.horariosLivres(List.of(i(400, 1100)), 480, 1080)).isEmpty();
    }

}
