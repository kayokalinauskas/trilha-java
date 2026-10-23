package dev.trilhajava.m10.aulas.desempenho;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OmissaoCoordenadaTest {

    @Test
    void oClienteFechadoRegistraAPausaUmaVezSo() {
        var resumo = ResumoDeLatencias.de(OmissaoCoordenada.medicaoDoClienteFechado());

        assertThat(resumo.amostras()).isEqualTo(5_700); // 300 envios previstos nunca aconteceram
        assertThat(resumo.p99Ms()).isEqualTo(2);
        assertThat(resumo.maximoMs()).isEqualTo(3_002);
    }

    @Test
    void peloHorarioPretendidoAPausaApareceNoP99() {
        var resumo = ResumoDeLatencias.de(OmissaoCoordenada.medicaoPeloHorarioPretendido());

        assertThat(resumo.amostras()).isEqualTo(6_000);
        assertThat(resumo.p50Ms()).isEqualTo(2);
        assertThat(resumo.p99Ms()).isEqualTo(2_402);
        assertThat(resumo.maximoMs()).isEqualTo(3_002);
    }
}
