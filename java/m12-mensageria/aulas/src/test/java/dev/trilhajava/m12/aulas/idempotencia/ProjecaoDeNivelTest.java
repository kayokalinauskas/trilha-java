package dev.trilhajava.m12.aulas.idempotencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Projeção de nível (idempotência natural com versão)")
class ProjecaoDeNivelTest {

    @Test
    @DisplayName("repetido e atrasado não mudam nada; só a versão maior vale")
    void versaoDescartaRepetidosEAtrasados() {
        var projecao = new ProjecaoDeNivel(new BancoDeTeste().jdbc);

        assertThat(projecao.aplicar(new NivelDoClienteAlterado("CLI-1", "PRATA", 1))).isTrue();
        assertThat(projecao.aplicar(new NivelDoClienteAlterado("CLI-1", "OURO", 3))).isTrue();
        assertThat(projecao.aplicar(new NivelDoClienteAlterado("CLI-1", "OURO", 3))).isFalse();     // repetido
        assertThat(projecao.aplicar(new NivelDoClienteAlterado("CLI-1", "BRONZE", 2))).isFalse();   // atrasado

        assertThat(projecao.nivelDe("CLI-1")).contains("OURO");
    }
}
