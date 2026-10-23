package dev.trilhajava.m10.aulas.tdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Os testes na ordem em que nasceram, um por ciclo de TDD (aula 10.06). */
class SimuladorDeParcelamentoTest {

    static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    static final Clock DIA_10_DE_MARCO = Clock.fixed(Instant.parse("2026-03-10T15:00:00Z"), SAO_PAULO);

    SimuladorDeParcelamento simulador = new SimuladorDeParcelamento(DIA_10_DE_MARCO);

    // Ciclo 1
    @Test
    void aVistaEhUmaParcelaComOTotal() {
        assertThat(valoresDe("100.00", 1)).containsExactly(reais("100.00"));
    }

    // Ciclo 2: triangulação
    @Test
    void duasParcelasDividemOTotalAoMeio() {
        assertThat(valoresDe("100.00", 2)).containsExactly(reais("50.00", "50.00"));
    }

    // Ciclo 3
    @Test
    void centavosQueSobramVaoParaAPrimeiraParcela() {
        assertThat(valoresDe("100.00", 3)).containsExactly(reais("33.34", "33.33", "33.33"));
    }

    // Ciclo 4: nasceu verde; foi "sabotado" para provar que consegue falhar
    @ParameterizedTest(name = "{0} em {1}x")
    @CsvSource({"100.00, 3", "999.99, 7", "1500.00, 12", "10.01, 2", "59.99, 11"})
    void aSomaDasParcelasEhSempreOTotal(String total, int quantidade) {
        BigDecimal soma = valoresDe(total, quantidade).stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(soma).isEqualByComparingTo(total);
    }

    // Ciclo 5
    @ParameterizedTest(name = "{0} parcelas")
    @ValueSource(ints = {0, -1, 13})
    void recusaQuantidadeForaDe1a12(int quantidade) {
        assertThatThrownBy(() -> simulador.simular(new BigDecimal("100.00"), quantidade))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("parcelas devem ser de 1 a 12, mas foram " + quantidade);
    }

    // Ciclo 6: o teste pediu um relógio, e o simulador passou a recebê-lo no construtor
    @Test
    void cadaParcelaVenceUmMesDepoisDaAnterior() {
        List<Parcela> parcelas = simulador.simular(new BigDecimal("300.00"), 3);

        assertThat(parcelas).extracting(Parcela::vencimento).containsExactly(
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 5, 10), LocalDate.of(2026, 6, 10));
    }

    // Ciclo 7: a borda do fim do mês
    @Test
    void compraNoDia31VenceNoUltimoDiaDosMesesMaisCurtos() {
        var simuladorNoDia31 = new SimuladorDeParcelamento(relogioEm(LocalDate.of(2026, 1, 31)));

        List<Parcela> parcelas = simuladorNoDia31.simular(new BigDecimal("300.00"), 3);

        assertThat(parcelas).extracting(Parcela::vencimento).containsExactly(
                LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 4, 30));
    }

    private List<BigDecimal> valoresDe(String total, int quantidade) {
        return simulador.simular(new BigDecimal(total), quantidade).stream()
                .map(Parcela::valor)
                .toList();
    }

    private static Clock relogioEm(LocalDate dia) {
        return Clock.fixed(dia.atTime(12, 0).atZone(SAO_PAULO).toInstant(), SAO_PAULO);
    }

    private static BigDecimal[] reais(String... valores) {
        return Arrays.stream(valores).map(BigDecimal::new).toArray(BigDecimal[]::new);
    }
}
