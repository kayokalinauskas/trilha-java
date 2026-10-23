package dev.trilhajava.m12.aulas.producao;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.apache.kafka.common.utils.Utils;

/**
 * As contas que decidem quantas partições um tópico precisa e o que acontece quando esse
 * número muda.
 */
public final class Particionamento {

    private Particionamento() {
    }

    /**
     * A partição de uma chave, com a mesma conta do particionador padrão do produtor
     * (murmur2 dos bytes da chave, positivo, módulo o número de partições).
     */
    public static int particaoDa(String chave, int particoes) {
        byte[] bytes = chave.getBytes(StandardCharsets.UTF_8);
        return Utils.toPositive(Utils.murmur2(bytes)) % particoes;
    }

    /** Fração das chaves que mudam de partição quando o tópico passa de {@code antes} para {@code depois}. */
    public static double fracaoRemapeada(Collection<String> chaves, int antes, int depois) {
        long mudaram = chaves.stream()
                .filter(chave -> particaoDa(chave, antes) != particaoDa(chave, depois))
                .count();
        return (double) mudaram / chaves.size();
    }

    /**
     * Partições necessárias para uma meta de vazão.
     *
     * @param alvoPorSegundo          mensagens por segundo no pico previsto
     * @param vazaoPorConsumidor      mensagens por segundo que UMA instância do consumidor processa
     *                                (medida num teste de carga, não estimada)
     * @param folgaPercentual         margem para crescimento e para reprocessar atraso (ex. 100 = o dobro)
     */
    public static int particoesNecessarias(long alvoPorSegundo, long vazaoPorConsumidor, int folgaPercentual) {
        if (alvoPorSegundo <= 0 || vazaoPorConsumidor <= 0 || folgaPercentual < 0) {
            throw new IllegalArgumentException("alvo e vazão precisam ser positivos; folga, zero ou mais");
        }
        long comFolga = alvoPorSegundo * (100 + folgaPercentual);
        long divisor = vazaoPorConsumidor * 100;
        return (int) ((comFolga + divisor - 1) / divisor);   // divisão arredondada para cima
    }
}
