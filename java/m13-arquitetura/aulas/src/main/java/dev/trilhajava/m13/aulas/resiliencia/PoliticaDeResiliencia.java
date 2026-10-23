package dev.trilhajava.m13.aulas.resiliencia;

import java.time.Duration;

/**
 * Todos os números das defesas num lugar só, para serem revistos juntos.
 * Os valores dependem uns dos outros: o orçamento total precisa caber no timeout de quem chama a clínica.
 */
public record PoliticaDeResiliencia(
        Duration timeoutDeConexao,
        Duration timeoutDeLeitura,
        int repeticoes,
        Duration esperaInicial,
        Duration jitter,
        Duration orcamentoTotal,
        int janelaDoCircuito,
        int minimoDeChamadas,
        float percentualDeFalhas,
        Duration tempoAberto) {

    /**
     * Produção. A conta do orçamento: 1 tentativa + 2 repetições, cada uma com até 800 ms de leitura,
     * mais 200 ms e 400 ms de espera (e até 100 ms de jitter em cada) dariam mais de 3 s no pior caso.
     * O orçamento de 2 s corta antes: a recepção não pode ficar esperando mais do que isso.
     */
    public static PoliticaDeResiliencia padrao() {
        return new PoliticaDeResiliencia(
                Duration.ofMillis(500), Duration.ofMillis(800),
                2, Duration.ofMillis(200), Duration.ofMillis(100), Duration.ofSeconds(2),
                20, 10, 50f, Duration.ofSeconds(30));
    }

    /** Valores curtos, para os testes e as demonstrações rodarem em segundos. */
    public static PoliticaDeResiliencia paraTestes() {
        return new PoliticaDeResiliencia(
                Duration.ofMillis(200), Duration.ofMillis(300),
                2, Duration.ofMillis(20), Duration.ofMillis(10), Duration.ofSeconds(2),
                4, 4, 50f, Duration.ofSeconds(30));
    }
}
