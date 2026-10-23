package dev.trilhajava.m11.aulas.owasp.erros;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import dev.trilhajava.m11.aulas.owasp.erros.AutorizadorDeEstorno.ServicoDeLimites;

class FalharFechadoTest {

    static final ServicoDeLimites LIMITE_DE_MIL = atendente -> new BigDecimal("1000.00");
    static final ServicoDeLimites FORA_DO_AR = atendente -> {
        throw new IllegalStateException("timeout de 2s no servico-de-limites");
    };

    static final BigDecimal ESTORNO_GRANDE = new BigDecimal("50000.00");

    @Test
    void comOServicoNoArAsDuasPoliticasConcordam() {
        assertThat(AutorizadorDeEstorno.falhandoAberto(LIMITE_DE_MIL).pode("atendente-7", ESTORNO_GRANDE)).isFalse();
        assertThat(AutorizadorDeEstorno.falhandoFechado(LIMITE_DE_MIL).pode("atendente-7", ESTORNO_GRANDE)).isFalse();
    }

    @Test
    void falharAbertoLiberaQualquerValorQuandoOServicoCai() {
        assertThat(AutorizadorDeEstorno.falhandoAberto(FORA_DO_AR).pode("atendente-7", ESTORNO_GRANDE)).isTrue();
    }

    @Test
    void falharFechadoNega() {
        assertThat(AutorizadorDeEstorno.falhandoFechado(FORA_DO_AR).pode("atendente-7", ESTORNO_GRANDE)).isFalse();
    }
}
