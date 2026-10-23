package dev.trilhajava.m00;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Exercício 00.01 — Saudação")
class SaudacaoTest {

    @Test
    @DisplayName("cumprimenta a pessoa pelo nome")
    void cumprimentaPeloNome() {
        assertThat(Saudacao.para("Ana")).isEqualTo("Olá, Ana!");
    }

    @Test
    @DisplayName("remove espaços extras antes e depois do nome")
    void removeEspacos() {
        assertThat(Saudacao.para("   Bruno  ")).isEqualTo("Olá, Bruno!");
    }

    @ParameterizedTest(name = "nome inválido [{0}] vira visitante")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("sem nome, cumprimenta um visitante")
    void semNome(String nome) {
        assertThat(Saudacao.para(nome)).isEqualTo("Olá, visitante!");
    }

    @Test
    @DisplayName("capitaliza a primeira letra de cada parte do nome")
    void capitaliza() {
        assertThat(Saudacao.para("maria clara")).isEqualTo("Olá, Maria Clara!");
        assertThat(Saudacao.para("JOÃO")).isEqualTo("Olá, João!");
    }
}
