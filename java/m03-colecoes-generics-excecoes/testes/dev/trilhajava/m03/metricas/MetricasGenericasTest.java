package dev.trilhajava.m03.metricas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exercício 03.04 — Métricas genéricas de sensores IoT")
class MetricasGenericasTest {

    private static final Instant T0 = Instant.parse("2026-05-01T10:00:00Z");

    @Nested
    @DisplayName("Estatisticas")
    class Stats {
        @Test
        @DisplayName("média aceita qualquer mistura de Number")
        void mediaMista() {
            List<Number> valores = List.of(1, 2.5, 3L, new BigDecimal("1.5"));
            assertThat(Estatisticas.media(valores)).isCloseTo(2.0, within(1e-9));
            List<Integer> inteiros = List.of(2, 4);
            assertThat(Estatisticas.media(inteiros)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("média de coleção vazia é erro")
        void mediaVazia() {
            assertThatThrownBy(() -> Estatisticas.media(List.of())).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("máximo funciona com qualquer Comparable — inclusive LocalDate (Comparable<ChronoLocalDate>)")
        void maximo() {
            assertThat(Estatisticas.maximo(List.of(3, 9, 4))).isEqualTo(9);
            assertThat(Estatisticas.maximo(List.of("pera", "uva", "abacaxi"))).isEqualTo("uva");
            LocalDate maisRecente = Estatisticas.maximo(List.of(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)));
            assertThat(maisRecente).isEqualTo(LocalDate.of(2026, 6, 30));
        }

        @Test
        @DisplayName("máximo de coleção vazia lança NoSuchElementException")
        void maximoVazio() {
            assertThatThrownBy(() -> Estatisticas.maximo(new ArrayList<Integer>())).isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("copiarAcimaDe: lê de List<Integer>, escreve em List<Number> (PECS)")
        void pecs() {
            List<Integer> origem = List.of(1, 7, 3, 12);
            List<Number> destino = new ArrayList<>();
            destino.add(0.5);
            Estatisticas.copiarAcimaDe(origem, destino, 5);
            assertThat(destino).containsExactly(0.5, 7, 12);

            List<Object> qualquerCoisa = new ArrayList<>();
            Estatisticas.copiarAcimaDe(List.of(10.5, 1.0), qualquerCoisa, 2);
            assertThat(qualquerCoisa).containsExactly(10.5);
        }

        @Test
        @DisplayName("média por sensor, com chaves em ordem alfabética")
        void mediaPorSensor() {
            List<Leitura<Integer>> leituras = List.of(
                    new Leitura<>("sala", T0, 20),
                    new Leitura<>("cozinha", T0, 25),
                    new Leitura<>("sala", T0.plusSeconds(60), 22));
            Map<String, Double> medias = Estatisticas.mediaPorSensor(leituras);
            assertThat(medias.keySet()).containsExactly("cozinha", "sala");
            assertThat(medias).containsEntry("sala", 21.0).containsEntry("cozinha", 25.0);

            List<Leitura<Double>> umidade = List.of(new Leitura<>("sala", T0, 55.5));
            assertThat(Estatisticas.mediaPorSensor(umidade)).containsEntry("sala", 55.5);
        }
    }

    @Nested
    @DisplayName("Janela<T> (buffer circular)")
    class JanelaDeslizante {
        @Test
        @DisplayName("guarda os N mais recentes, do mais antigo ao mais novo")
        void deslizante() {
            Janela<Integer> janela = new Janela<>(3);
            for (int i = 1; i <= 5; i++) {
                janela.adicionar(i);
            }
            assertThat(janela.ultimos()).containsExactly(3, 4, 5);
            assertThat(janela.tamanho()).isEqualTo(3);
            assertThat(janela.estaCheia()).isTrue();
        }

        @Test
        @DisplayName("antes de encher, devolve só o que tem")
        void parcial() {
            Janela<String> janela = new Janela<>(4);
            janela.adicionar("a");
            janela.adicionar("b");
            assertThat(janela.ultimos()).containsExactly("a", "b");
            assertThat(janela.estaCheia()).isFalse();
        }

        @Test
        @DisplayName("funciona com leituras e integra com Estatisticas")
        void integracao() {
            Janela<Leitura<Double>> janela = new Janela<>(2);
            janela.adicionar(new Leitura<>("s", T0, 10.0));
            janela.adicionar(new Leitura<>("s", T0, 20.0));
            janela.adicionar(new Leitura<>("s", T0, 40.0));
            assertThat(Estatisticas.mediaPorSensor(janela.ultimos())).containsEntry("s", 30.0);
        }

        @Test
        @DisplayName("capacidade inválida e elemento nulo são recusados")
        void validacoes() {
            assertThatThrownBy(() -> new Janela<Integer>(0)).isInstanceOf(IllegalArgumentException.class);
            Janela<Integer> janela = new Janela<>(1);
            assertThatThrownBy(() -> janela.adicionar(null)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("a lista devolvida é uma cópia imutável")
        void copia() {
            Janela<Integer> janela = new Janela<>(2);
            janela.adicionar(1);
            List<Integer> foto = janela.ultimos();
            janela.adicionar(2);
            assertThat(foto).containsExactly(1);
            assertThatThrownBy(() -> foto.add(9)).isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
