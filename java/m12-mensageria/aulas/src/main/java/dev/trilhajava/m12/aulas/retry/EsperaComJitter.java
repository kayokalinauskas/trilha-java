package dev.trilhajava.m12.aulas.retry;

import java.time.Duration;
import java.util.random.RandomGenerator;

/**
 * Backoff exponencial com teto, sem e com "full jitter" (espera sorteada entre zero e o valor exponencial).
 * O main simula 1.000 consumidores que falharam no mesmo instante e mostra quando cada um tenta de novo.
 */
public final class EsperaComJitter {

    private final long baseMs;
    private final long tetoMs;
    private final RandomGenerator aleatorio;

    public EsperaComJitter(Duration base, Duration teto, RandomGenerator aleatorio) {
        this.baseMs = base.toMillis();
        this.tetoMs = teto.toMillis();
        this.aleatorio = aleatorio;
    }

    /** base × 2^(tentativa − 1), limitado ao teto. A tentativa 1 é a primeira repetição. */
    public Duration semJitter(int tentativa) {
        if (tentativa < 1) {
            throw new IllegalArgumentException("a tentativa começa em 1: " + tentativa);
        }
        int expoente = Math.min(tentativa - 1, 30); // 2^30 já passa de qualquer teto razoável
        return Duration.ofMillis(Math.min(tetoMs, baseMs << expoente));
    }

    /** Full jitter: um valor sorteado entre 0 e o valor exponencial (inclusive). */
    public Duration comJitterTotal(int tentativa) {
        return Duration.ofMillis(aleatorio.nextLong(semJitter(tentativa).toMillis() + 1));
    }

    public static void main(String[] args) {
        var espera = new EsperaComJitter(Duration.ofMillis(200), Duration.ofSeconds(30), RandomGenerator.of("L64X128MixRandom"));
        int consumidores = 1_000;
        int tentativa = 4; // a 4ª repetição: 200 × 2³ = 1.600 ms
        int[] semJitter = new int[4];
        int[] comJitter = new int[4];
        for (int i = 0; i < consumidores; i++) {
            semJitter[faixa(espera.semJitter(tentativa))]++;
            comJitter[faixa(espera.comJitterTotal(tentativa))]++;
        }
        System.out.printf("Tentativa %d de %d consumidores (espera exponencial: %d ms)%n",
                tentativa, consumidores, espera.semJitter(tentativa).toMillis());
        System.out.println("faixa de tempo      sem jitter   com jitter");
        String[] rotulos = {"0 a 399 ms", "400 a 799 ms", "800 a 1199 ms", "1200 a 1600 ms"};
        for (int f = 0; f < 4; f++) {
            System.out.printf("%-18s %11d %12d%n", rotulos[f], semJitter[f], comJitter[f]);
        }
    }

    private static int faixa(Duration espera) {
        return (int) Math.min(3, espera.toMillis() / 400);
    }
}
