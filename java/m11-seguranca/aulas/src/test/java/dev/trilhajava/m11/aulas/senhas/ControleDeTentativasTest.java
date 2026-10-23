package dev.trilhajava.m11.aulas.senhas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ControleDeTentativasTest {

    private static final Instant INICIO = Instant.parse("2026-10-05T13:00:00Z");
    private final RelogioDeTeste relogio = new RelogioDeTeste(INICIO);
    private final ControleDeTentativas controle = new ControleDeTentativas(relogio);

    private void falhar(int vezes) {
        for (int i = 0; i < vezes; i++) {
            controle.registrarFalha("marta@email.com");
        }
    }

    @Test
    @DisplayName("As 3 primeiras falhas são livres")
    void tresLivres() {
        falhar(3);

        assertThat(controle.esperaAte("marta@email.com")).isEmpty();
    }

    @Test
    @DisplayName("A 4ª falha impõe 30 segundos de espera")
    void quartaFalha() {
        falhar(4);

        assertThat(controle.esperaAte("marta@email.com")).contains(INICIO.plusSeconds(30));
        relogio.avancar(Duration.ofSeconds(29));
        assertThat(controle.esperaAte("marta@email.com")).isPresent();
        relogio.avancar(Duration.ofSeconds(1));
        assertThat(controle.esperaAte("marta@email.com")).isEmpty();
    }

    @Test
    @DisplayName("A espera dobra a cada falha e para em 15 minutos")
    void esperaDobra() {
        assertThat(ControleDeTentativas.espera(4)).isEqualTo(Duration.ofSeconds(30));
        assertThat(ControleDeTentativas.espera(5)).isEqualTo(Duration.ofMinutes(1));
        assertThat(ControleDeTentativas.espera(6)).isEqualTo(Duration.ofMinutes(2));
        assertThat(ControleDeTentativas.espera(8)).isEqualTo(Duration.ofMinutes(8));
        assertThat(ControleDeTentativas.espera(9)).isEqualTo(Duration.ofMinutes(15));
        assertThat(ControleDeTentativas.espera(99)).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("Cada e-mail tem a sua contagem")
    void porEmail() {
        falhar(4);

        assertThat(controle.esperaAte("joao@email.com")).isEmpty();
    }

    @Test
    @DisplayName("Com 100 falhas seguidas, só a recuperação de senha libera")
    void limiteAbsoluto() {
        falhar(99);
        assertThat(controle.exigeRecuperacao("marta@email.com")).isFalse();

        falhar(1);
        assertThat(controle.exigeRecuperacao("marta@email.com")).isTrue();

        controle.limpar("marta@email.com");
        assertThat(controle.exigeRecuperacao("marta@email.com")).isFalse();
    }
}
