package dev.trilhajava.m13.cambio;

import java.time.Duration;

/**
 * @param timeoutDeConexao     quanto esperar para abrir a conexão
 * @param timeoutDeLeitura     quanto esperar pela resposta
 * @param maxRetries           repetições depois da 1ª tentativa, só para falhas transitórias
 * @param atrasoInicial        espera antes da 1ª repetição; dobra a cada uma
 * @param janelaDoCircuito     quantas chamadas o circuit breaker observa (e o mínimo para decidir)
 * @param percentualDeFalhas   a partir deste percentual de falhas na janela, o circuito abre
 * @param tempoAberto          quanto tempo o circuito fica aberto antes de deixar uma chamada de teste passar
 * @param validadeDoUltimoValor até que idade o último valor conhecido pode ser devolvido como fallback
 */
public record ConfiguracaoDoCliente(
        Duration timeoutDeConexao,
        Duration timeoutDeLeitura,
        int maxRetries,
        Duration atrasoInicial,
        int janelaDoCircuito,
        float percentualDeFalhas,
        Duration tempoAberto,
        Duration validadeDoUltimoValor) {

    public static ConfiguracaoDoCliente padrao() {
        return new ConfiguracaoDoCliente(Duration.ofSeconds(1), Duration.ofSeconds(2), 2, Duration.ofMillis(200),
                10, 50f, Duration.ofSeconds(30), Duration.ofMinutes(15));
    }
}
