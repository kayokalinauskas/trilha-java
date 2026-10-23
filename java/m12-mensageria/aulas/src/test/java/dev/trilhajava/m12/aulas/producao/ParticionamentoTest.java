package dev.trilhajava.m12.aulas.producao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class ParticionamentoTest {

    @Test
    void aumentarAsParticoesMudaAParticaoDaMaioriaDasChaves() {
        List<String> contas = IntStream.rangeClosed(1, 10_000).mapToObj(n -> "conta-" + n).toList();

        double de6Para8 = Particionamento.fracaoRemapeada(contas, 6, 8);
        double de6Para12 = Particionamento.fracaoRemapeada(contas, 6, 12);

        System.out.printf("conta-42 em 6 partições: p%d; em 8: p%d%n",
                Particionamento.particaoDa("conta-42", 6), Particionamento.particaoDa("conta-42", 8));
        System.out.printf("6 -> 8: %.1f%% das chaves mudam de partição%n", de6Para8 * 100);
        System.out.printf("6 -> 12: %.1f%% das chaves mudam de partição%n", de6Para12 * 100);

        assertThat(de6Para8).isGreaterThan(0.5);
        // Dobrar (6 -> 12) mantém cada chave em p ou p + 6: metade fica, metade muda.
        assertThat(de6Para12).isBetween(0.45, 0.55);
    }

    @Test
    void dimensionaPelaVazaoMedidaDoConsumidorComFolga() {
        // Pico de 3.000 pagamentos/s; uma instância do consumidor processa 400/s; folga de 100%.
        assertThat(Particionamento.particoesNecessarias(3_000, 400, 100)).isEqualTo(15);
        // Sem folga, 7,5 vira 8: não existe meia partição.
        assertThat(Particionamento.particoesNecessarias(3_000, 400, 0)).isEqualTo(8);
    }
}
