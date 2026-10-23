package dev.trilhajava.m04.credito;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 04.03 — Motor de regras de crédito com funções")
class MotorCreditoTest {

    private static Proposta proposta(int idade, String renda, int score, String valor, int parcelas, boolean negativado) {
        return new Proposta("52998224725", idade, new BigDecimal(renda), score, new BigDecimal(valor), parcelas, negativado);
    }

    private final Proposta boa = proposta(30, "5000", 750, "12000", 12, false);

    @Test
    @DisplayName("fábricas de predicados isolados")
    void predicados() {
        assertThat(MotorCredito.idadeEntre(18, 75).test(boa)).isTrue();
        assertThat(MotorCredito.idadeEntre(18, 75).test(proposta(17, "5000", 750, "100", 1, false))).isFalse();
        assertThat(MotorCredito.idadeEntre(18, 75).test(proposta(75, "5000", 750, "100", 1, false))).as("limites inclusivos").isTrue();
        assertThat(MotorCredito.rendaMinima(new BigDecimal("1500")).test(boa)).isTrue();
        assertThat(MotorCredito.scoreMinimo(800).test(boa)).isFalse();
        assertThat(MotorCredito.semNegativacao().test(proposta(30, "5000", 750, "100", 1, true))).isFalse();
    }

    @Test
    @DisplayName("comprometimento: parcela estimada até X% da renda")
    void comprometimento() {
        // parcela 1000 = 20% de 5000
        assertThat(MotorCredito.comprometimentoMaximo(new BigDecimal("0.30")).test(boa)).isTrue();
        assertThat(MotorCredito.comprometimentoMaximo(new BigDecimal("0.20")).test(boa)).as("exatamente no limite").isTrue();
        assertThat(MotorCredito.comprometimentoMaximo(new BigDecimal("0.19")).test(boa)).isFalse();
    }

    @Test
    @DisplayName("avaliar aplica TODAS as regras e lista as reprovadas na ordem")
    void avaliar() {
        List<Regra> politica = MotorCredito.politicaPadrao();
        assertThat(MotorCredito.avaliar(boa, politica)).isEqualTo(new Avaliacao(true, List.of()));

        Proposta ruim = proposta(17, "1000", 400, "12000", 12, true);
        Avaliacao a = MotorCredito.avaliar(ruim, politica);
        assertThat(a.aprovada()).isFalse();
        assertThat(a.regrasReprovadas()).containsExactly("idade", "renda mínima", "score", "sem negativação", "comprometimento de renda");
    }

    @Test
    @DisplayName("sem regras, a proposta é aprovada")
    void semRegras() {
        assertThat(MotorCredito.avaliar(boa, List.of()).aprovada()).isTrue();
    }

    @Test
    @DisplayName("todas() combina a lista em um único predicado")
    void todas() {
        Predicate<Proposta> tudo = MotorCredito.todas(MotorCredito.politicaPadrao());
        assertThat(tudo.test(boa)).isTrue();
        assertThat(tudo.test(proposta(30, "5000", 750, "12000", 12, true))).isFalse();
        assertThat(MotorCredito.todas(List.of()).test(boa)).isTrue();
    }

    @Test
    @DisplayName("limite sugerido depende da faixa de score")
    void limite() {
        assertThat(MotorCredito.limiteSugerido(proposta(30, "4000", 900, "1", 1, false))).isEqualByComparingTo("24000");
        assertThat(MotorCredito.limiteSugerido(proposta(30, "4000", 700, "1", 1, false))).isEqualByComparingTo("12000");
        assertThat(MotorCredito.limiteSugerido(proposta(30, "4000", 550, "1", 1, false))).isEqualByComparingTo("4000");
        assertThat(MotorCredito.limiteSugerido(proposta(30, "4000", 499, "1", 1, false))).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("regras novas entram sem mudar o motor")
    void extensivel() {
        Regra maximoDeParcelas = new Regra("parcelas", p -> p.parcelas() <= 24);
        Avaliacao a = MotorCredito.avaliar(proposta(30, "5000", 750, "12000", 36, false), List.of(maximoDeParcelas));
        assertThat(a.regrasReprovadas()).containsExactly("parcelas");
    }
}
