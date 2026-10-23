package dev.trilhajava.m12.outbox;

import java.time.Duration;

/**
 * @param tamanhoDoLote  quantos pendentes o relay lê por execução
 * @param maxTentativas  quantas falhas até o evento virar MORTO
 * @param atrasoInicial  espera depois da 1ª falha; dobra a cada falha seguinte
 * @param atrasoMaximo   teto da espera
 */
public record ConfiguracaoDoRelay(int tamanhoDoLote, int maxTentativas, Duration atrasoInicial, Duration atrasoMaximo) {

    public ConfiguracaoDoRelay {
        if (tamanhoDoLote <= 0 || maxTentativas <= 0) {
            throw new IllegalArgumentException("lote e tentativas precisam ser positivos");
        }
        if (atrasoInicial.isNegative() || atrasoInicial.isZero() || atrasoMaximo.compareTo(atrasoInicial) < 0) {
            throw new IllegalArgumentException("atrasos inválidos");
        }
    }

    public static ConfiguracaoDoRelay padrao() {
        return new ConfiguracaoDoRelay(100, 10, Duration.ofSeconds(1), Duration.ofMinutes(5));
    }
}
