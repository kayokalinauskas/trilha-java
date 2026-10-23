package dev.trilhajava.m05;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Exercício 05.04 — Busca binária (inclusive na resposta)")
class BuscaBinariaTest {

    @ParameterizedTest(name = "inserir {0} em [1,3,3,5,8] → {1}")
    @CsvSource({"0, 0", "1, 0", "3, 1", "4, 3", "8, 4", "9, 5"})
    void posicaoDeInsercao(int alvo, int esperado) {
        assertThat(BuscaBinaria.posicaoDeInsercao(new int[] {1, 3, 3, 5, 8}, alvo)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("primeira versão com bug usando no máximo ~log2(n) chamadas")
    void primeiraComBug() {
        AtomicInteger chamadas = new AtomicInteger();
        int resultado = BuscaBinaria.primeiraVersaoComBug(2_000_000_000, v -> {
            chamadas.incrementAndGet();
            return v >= 1_234_567_890;
        });
        assertThat(resultado).isEqualTo(1_234_567_890);
        assertThat(chamadas.get()).isLessThanOrEqualTo(33);
        assertThat(BuscaBinaria.primeiraVersaoComBug(5, v -> v >= 1)).isEqualTo(1);
        assertThat(BuscaBinaria.primeiraVersaoComBug(5, v -> false)).as("sem bug").isEqualTo(-1);
    }

    @Test
    @DisplayName("busca em array ordenado e rotacionado")
    void rotacionado() {
        int[] nums = {15, 18, 22, 3, 7, 9, 12};
        assertThat(BuscaBinaria.buscarEmRotacionado(nums, 7)).isEqualTo(4);
        assertThat(BuscaBinaria.buscarEmRotacionado(nums, 18)).isEqualTo(1);
        assertThat(BuscaBinaria.buscarEmRotacionado(nums, 10)).isEqualTo(-1);
        assertThat(BuscaBinaria.buscarEmRotacionado(new int[] {1, 2, 3}, 3)).isEqualTo(2);
        assertThat(BuscaBinaria.buscarEmRotacionado(new int[] {}, 3)).isEqualTo(-1);
    }

    @Test
    @DisplayName("menor capacidade do caminhão para entregar tudo em D dias")
    void capacidade() {
        assertThat(BuscaBinaria.menorCapacidade(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 5)).isEqualTo(15);
        assertThat(BuscaBinaria.menorCapacidade(new int[] {3, 2, 2, 4, 1, 4}, 3)).isEqualTo(6);
        assertThat(BuscaBinaria.menorCapacidade(new int[] {7}, 10)).isEqualTo(7);
    }
}
