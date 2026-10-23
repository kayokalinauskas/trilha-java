package dev.trilhajava.m11.aulas.defesas;

import java.time.Duration;
import java.time.Instant;

/**
 * Mesma rajada, dois algoritmos, ambos com "10 por minuto": um cliente manda 10 requisições aos
 * 59,5 s e mais 10 aos 60,5 s. Quantas passam em cada um?
 */
public class SimulacaoDaVirada {

    public static void main(String[] args) {
        Instant inicio = Instant.parse("2026-10-05T12:00:00Z");

        RelogioAjustavel relogioDaJanela = new RelogioAjustavel(inicio);
        JanelaFixa janela = new JanelaFixa(10, Duration.ofMinutes(1), relogioDaJanela);

        RelogioAjustavel relogioDoBalde = new RelogioAjustavel(inicio);
        LimitadorDeCreditos balde = new LimitadorDeCreditos(
                new PoliticaDeLimite("por-cliente", 10, Duration.ofMinutes(1)), relogioDoBalde);

        relogioDaJanela.avancar(Duration.ofMillis(59_500));
        relogioDoBalde.avancar(Duration.ofMillis(59_500));
        int janelaAntes = rajada(janela);
        int baldeAntes = rajada(balde);

        relogioDaJanela.avancar(Duration.ofSeconds(1));
        relogioDoBalde.avancar(Duration.ofSeconds(1));
        int janelaDepois = rajada(janela);
        int baldeDepois = rajada(balde);

        System.out.printf("%-14s %8s %8s %8s%n", "algoritmo", "59,5 s", "60,5 s", "total");
        System.out.printf("%-14s %8d %8d %8d%n", "janela fixa", janelaAntes, janelaDepois, janelaAntes + janelaDepois);
        System.out.printf("%-14s %8d %8d %8d%n", "token bucket", baldeAntes, baldeDepois, baldeAntes + baldeDepois);
    }

    private static int rajada(JanelaFixa janela) {
        int aceitas = 0;
        for (int i = 0; i < 10; i++) {
            if (janela.tentar()) {
                aceitas++;
            }
        }
        return aceitas;
    }

    private static int rajada(LimitadorDeCreditos balde) {
        int aceitas = 0;
        for (int i = 0; i < 10; i++) {
            if (balde.consumir("cliente-1", 1).permitido()) {
                aceitas++;
            }
        }
        return aceitas;
    }
}
