package dev.trilhajava.m10.aulas.desempenho;

/**
 * O jeito que quase todo mundo tenta primeiro: System.nanoTime() em volta de um laço.
 * Rodamos o MESMO laço dez vezes seguidas, na mesma JVM, e imprimimos o tempo de cada rodada.
 *
 * <p>Rode com: {@code mvn test-compile exec:exec@ingenua}
 */
public class MedicaoIngenua {

    public static void main(String[] args) {
        double soma = 0;
        for (int rodada = 1; rodada <= 10; rodada++) {
            long inicio = System.nanoTime();
            for (int i = 0; i < 10_000; i++) {
                soma += Distancia.km(-23.3550, -46.8770, -23.5614 + i * 1e-6, -46.6559);
            }
            long nanos = System.nanoTime() - inicio;
            System.out.printf("Rodada %2d: %6.1f ns por cálculo%n", rodada, nanos / 10_000.0);
        }
        // Usar a soma impede que o JIT descarte o laço inteiro (veja ArmadilhasDoJitBenchmark).
        System.out.printf("(soma de controle: %.0f km)%n", soma);
    }
}
