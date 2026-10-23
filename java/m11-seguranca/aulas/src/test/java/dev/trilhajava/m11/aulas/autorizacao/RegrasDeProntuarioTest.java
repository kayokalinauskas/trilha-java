package dev.trilhajava.m11.aulas.autorizacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** As regras sem HTTP, sem Spring e sem token: milissegundos por teste. */
class RegrasDeProntuarioTest {

    final RegrasDeProntuario regras = new RegrasDeProntuario(new RepositorioDeProntuarios());

    final Prontuario daAna = new Prontuario(1001, "sol", "pac-ana", "med-carla", "Dor de cabeça", "Enxaqueca");

    @Test
    @DisplayName("a clínica vem antes do papel: recepção de outra clínica não vê")
    void tenantPrimeiro() {
        assertThat(regras.podeVer(new Solicitante("atd-iara", "lua", Papel.ATENDENTE), daAna)).isFalse();
        assertThat(regras.podeVer(new Solicitante("atd-elisa", "sol", Papel.ATENDENTE), daAna)).isTrue();
    }

    @Test
    @DisplayName("mesmo id de usuário em outra clínica não vale: o tenant faz parte da identidade")
    void mesmoUsuarioOutroTenant() {
        assertThat(regras.podeVer(new Solicitante("pac-ana", "lua", Papel.PACIENTE), daAna)).isFalse();
    }

    @Test
    @DisplayName("recepção vê o resumo, mas não as anotações; médico responsável anota")
    void anotacoes() {
        assertThat(regras.veAnotacoes(new Solicitante("atd-elisa", "sol", Papel.ATENDENTE), daAna)).isFalse();
        assertThat(regras.podeEditarAnotacoes(new Solicitante("med-carla", "sol", Papel.MEDICO), daAna)).isTrue();
        assertThat(regras.podeEditarAnotacoes(new Solicitante("med-diego", "sol", Papel.MEDICO), daAna)).isFalse();
    }
}
